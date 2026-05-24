package com.escuela.estudiantes.service;

import com.escuela.estudiantes.entity.Estudiante;
import com.escuela.estudiantes.feign.CobrosClient;
import com.escuela.estudiantes.repository.EstudianteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Aplica transiciones automaticas de estado academico y de situacion_pago
 * sobre {@link Estudiante} en respuesta a eventos externos (pago.registrado,
 * asignacion.creada).
 *
 * <p>Todos los metodos son <b>idempotentes</b>: si la transicion no aplica
 * para el estado actual, se hace skip silencioso. Esto permite re-procesar
 * eventos sin riesgo (necesario porque los listeners de RabbitMQ pueden
 * reentregar mensajes ante retries).</p>
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class EstudianteEstadoService {

    private final EstudianteRepository repository;
    private final ObjectProvider<CobrosClient> cobrosClientProvider;

    /**
     * Reacciona a un evento {@code pago.registrado}.
     *
     * <ul>
     *   <li>Si el estudiante esta {@code PRE_MATRICULADO} → pasa a
     *       {@code MATRICULADO} y se setea {@code fechaMatricula = hoy}.</li>
     *   <li>Actualiza {@code situacion_pago} segun {@code estadoFactura}
     *       resultante del pago:
     *       <ul>
     *         <li>{@code PAGADA} → {@code PAGADO_TOTAL}</li>
     *         <li>{@code PARCIAL} → {@code PAGO_PARCIAL}</li>
     *       </ul>
     *   </li>
     * </ul>
     *
     * @param estudianteId   id del estudiante
     * @param estadoFactura  estado de la factura luego del pago ({@code PAGADA}
     *                       o {@code PARCIAL})
     */
    public void procesarPagoRegistrado(Long estudianteId, String estadoFactura) {
        Optional<Estudiante> opt = repository.findByIdAndDeletedAtIsNull(estudianteId);
        if (opt.isEmpty()) {
            log.warn("procesarPagoRegistrado: estudiante {} no encontrado", estudianteId);
            return;
        }
        Estudiante e = opt.get();
        boolean dirty = false;

        // Auto-transicion academica al primer pago
        if ("PRE_MATRICULADO".equals(e.getEstado())) {
            e.setEstado("MATRICULADO");
            e.setFechaMatricula(LocalDate.now());
            dirty = true;
            log.info("Estudiante {} PRE_MATRICULADO → MATRICULADO (primer pago)", estudianteId);
        }

        // Situacion financiera
        String nuevaSituacion = mapearSituacionPago(estadoFactura);
        if (nuevaSituacion != null && !nuevaSituacion.equals(e.getSituacionPago())) {
            e.setSituacionPago(nuevaSituacion);
            dirty = true;
            log.info("Estudiante {} situacion_pago → {}", estudianteId, nuevaSituacion);
        }

        if (dirty) {
            repository.save(e);
        }
    }

    /**
     * Reacciona a un evento {@code asignacion.creada}.
     * Si el estudiante esta {@code MATRICULADO} → pasa a {@code CURSANDO}
     * (porque ya tiene su primera clase asignada).
     */
    public void procesarAsignacionCreada(Long estudianteId) {
        Optional<Estudiante> opt = repository.findByIdAndDeletedAtIsNull(estudianteId);
        if (opt.isEmpty()) {
            log.warn("procesarAsignacionCreada: estudiante {} no encontrado", estudianteId);
            return;
        }
        Estudiante e = opt.get();

        if ("MATRICULADO".equals(e.getEstado())) {
            e.setEstado("CURSANDO");
            repository.save(e);
            log.info("Estudiante {} MATRICULADO → CURSANDO (primera asignacion)", estudianteId);
        }
        // Si ya esta CURSANDO/COMPLETADO/RETIRADO no hacemos nada (idempotente).
    }

    /**
     * Sincroniza la {@code situacion_pago} de TODOS los estudiantes consultando
     * a MS-Cobros vía Feign. Reservado para uso administrativo (resolver drift
     * tras caídas de servicio o eventos perdidos antes de que la cola existiera).
     *
     * @return map con conteos: {@code total}, {@code actualizados}, {@code errores}
     */
    public Map<String, Integer> sincronizarSituacionPagoMasivo() {
        CobrosClient cobros = cobrosClientProvider.getIfAvailable();
        if (cobros == null) {
            log.warn("CobrosClient no disponible, sincronizacion abortada");
            return Map.of("total", 0, "actualizados", 0, "errores", 0);
        }

        List<Estudiante> todos = repository.findAll().stream()
            .filter(e -> e.getDeletedAt() == null)
            .toList();

        AtomicInteger actualizados = new AtomicInteger();
        AtomicInteger errores = new AtomicInteger();

        for (Estudiante e : todos) {
            try {
                Map<String, String> resp = cobros.obtenerSituacionPago(e.getId());
                String nueva = ajustarSegunEstado(e, resp != null ? resp.get("situacionPago") : null);
                if (nueva != null && !nueva.equals(e.getSituacionPago())) {
                    String anterior = e.getSituacionPago();
                    e.setSituacionPago(nueva);
                    repository.save(e);
                    actualizados.incrementAndGet();
                    log.info("Estudiante {} situacion_pago {} → {}", e.getId(), anterior, nueva);
                }
            } catch (Exception ex) {
                errores.incrementAndGet();
                log.warn("Error sincronizando estudiante {}: {}", e.getId(), ex.getMessage());
            }
        }

        return Map.of("total", todos.size(),
                      "actualizados", actualizados.get(),
                      "errores", errores.get());
    }

    /** Sincroniza solo un estudiante. Útil para refresco puntual desde UI. */
    public String sincronizarSituacionPago(Long estudianteId) {
        CobrosClient cobros = cobrosClientProvider.getIfAvailable();
        if (cobros == null) return null;
        Optional<Estudiante> opt = repository.findByIdAndDeletedAtIsNull(estudianteId);
        if (opt.isEmpty()) return null;
        Estudiante e = opt.get();
        Map<String, String> resp = cobros.obtenerSituacionPago(estudianteId);
        String nueva = ajustarSegunEstado(e, resp != null ? resp.get("situacionPago") : null);
        if (nueva == null) return e.getSituacionPago();
        if (!nueva.equals(e.getSituacionPago())) {
            e.setSituacionPago(nueva);
            repository.save(e);
            log.info("Estudiante {} situacion_pago sincronizada → {}", estudianteId, nueva);
        }
        return nueva;
    }

    /**
     * Combina la situacion calculada por MS-Cobros con el estado academico
     * del estudiante. La regla especial: PRE_MATRICULADO + SIN_DEUDA significa
     * "registrado pero falta cobrarle matricula" — lo mapeamos a
     * PENDIENTE_MATRICULA para que destaque en la UI.
     */
    private String ajustarSegunEstado(Estudiante e, String situacionCobros) {
        if (situacionCobros == null) return null;
        if ("SIN_DEUDA".equals(situacionCobros) && "PRE_MATRICULADO".equals(e.getEstado())) {
            return "PENDIENTE_MATRICULA";
        }
        return situacionCobros;
    }

    private String mapearSituacionPago(String estadoFactura) {
        if (estadoFactura == null) return null;
        return switch (estadoFactura) {
            case "PAGADA", "PAGADO" -> "PAGADO_TOTAL";
            case "PARCIAL"          -> "PAGO_PARCIAL";
            // PENDIENTE/VENCIDA/ANULADA no afectan situacion via este evento
            default                 -> null;
        };
    }
}
