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
     * <p><b>Modelo nuevo (Sprint 9 ext):</b> la transicion
     * {@code PRE_MATRICULADO → MATRICULADO} solo ocurre cuando el estudiante
     * queda en {@code PAGADO_TOTAL} (saldo financiero = 0). Pagos parciales
     * NO matriculan; el estudiante sigue en PRE_MATRICULADO hasta saldar.
     *
     * <ul>
     *   <li>{@code estadoFactura = PAGADA} → {@code situacion_pago = PAGADO_TOTAL}
     *       + si PRE_MATRICULADO → MATRICULADO + {@code fechaMatricula = hoy}</li>
     *   <li>{@code estadoFactura = PARCIAL} → {@code situacion_pago = PAGO_PARCIAL}
     *       (NO matricula)</li>
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

        // Situacion financiera
        String nuevaSituacion = mapearSituacionPago(estadoFactura);
        if (nuevaSituacion != null && !nuevaSituacion.equals(e.getSituacionPago())) {
            e.setSituacionPago(nuevaSituacion);
            dirty = true;
            log.info("Estudiante {} situacion_pago → {}", estudianteId, nuevaSituacion);
        }

        // Auto-transicion academica solo si quedo PAGADO_TOTAL
        if ("PRE_MATRICULADO".equals(e.getEstado())
                && "PAGADO_TOTAL".equals(e.getSituacionPago())) {
            e.setEstado("MATRICULADO");
            e.setFechaMatricula(LocalDate.now());
            dirty = true;
            log.info("Estudiante {} PRE_MATRICULADO → MATRICULADO (saldo cero)", estudianteId);
        }

        if (dirty) {
            repository.save(e);
        }
    }

    /**
     * Reacciona a un evento {@code factura.emitida}.
     *
     * <ul>
     *   <li>Factura {@code CONTADO} emitida → {@code situacion_pago = PENDIENTE_PAGO}
     *       (factura existe pero $0 pagado). NO matricula.</li>
     *   <li>Factura {@code CREDITO} emitida → {@code situacion_pago = PAGADO_TOTAL}
     *       (asume cobro automatico por tarjeta). Si era PRE_MATRICULADO →
     *       MATRICULADO con {@code fechaMatricula = hoy}.</li>
     * </ul>
     *
     * <p>Idempotente: si la situacion ya esta en el valor objetivo, no hace nada.
     */
    public void procesarFacturaEmitida(Long estudianteId, String tipoPago) {
        Optional<Estudiante> opt = repository.findByIdAndDeletedAtIsNull(estudianteId);
        if (opt.isEmpty()) {
            log.warn("procesarFacturaEmitida: estudiante {} no encontrado", estudianteId);
            return;
        }
        Estudiante e = opt.get();
        boolean dirty = false;

        String nuevaSituacion;
        if ("CREDITO".equalsIgnoreCase(tipoPago)) {
            nuevaSituacion = "PAGADO_TOTAL";
        } else {
            // CONTADO o cualquier otro tipo
            nuevaSituacion = "PENDIENTE_PAGO";
        }

        if (!nuevaSituacion.equals(e.getSituacionPago())) {
            e.setSituacionPago(nuevaSituacion);
            dirty = true;
            log.info("Estudiante {} situacion_pago → {} (factura emitida tipo={})",
                    estudianteId, nuevaSituacion, tipoPago);
        }

        // Auto-transicion: si la emision dejo en PAGADO_TOTAL (caso CREDITO),
        // matricular al PRE_MATRICULADO.
        if ("PRE_MATRICULADO".equals(e.getEstado())
                && "PAGADO_TOTAL".equals(e.getSituacionPago())) {
            e.setEstado("MATRICULADO");
            e.setFechaMatricula(LocalDate.now());
            dirty = true;
            log.info("Estudiante {} PRE_MATRICULADO → MATRICULADO (CREDITO emitido)", estudianteId);
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
     * del estudiante. Con el modelo simplificado (Sprint 9 ext), ms-cobros
     * ya devuelve directamente los 4 valores correctos
     * (PENDIENTE_FACTURACION, PENDIENTE_PAGO, PAGO_PARCIAL, PAGADO_TOTAL),
     * por lo que aqui solo pasamos el valor tal cual.
     */
    private String ajustarSegunEstado(Estudiante e, String situacionCobros) {
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
