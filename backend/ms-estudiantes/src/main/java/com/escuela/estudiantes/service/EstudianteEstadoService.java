package com.escuela.estudiantes.service;

import com.escuela.estudiantes.entity.Estudiante;
import com.escuela.estudiantes.repository.EstudianteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

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
