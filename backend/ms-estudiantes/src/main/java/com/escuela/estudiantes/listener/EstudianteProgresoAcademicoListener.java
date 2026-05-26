package com.escuela.estudiantes.listener;

import com.escuela.common.events.asignaciones.AsignacionCanceladaEvent;
import com.escuela.common.events.asignaciones.AsignacionCompletadaEvent;
import com.escuela.common.events.asignaciones.AsignacionCreadaEvent;
import com.escuela.estudiantes.config.RabbitConfig;
import com.escuela.estudiantes.service.ProgresoAcademicoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Consume el ciclo de vida de las asignaciones para mantener la tabla
 * {@code progreso_academico} con contadores agregados sin queries pesadas:
 *
 * <ul>
 *   <li>{@code asignacion.creada}    &rarr; clasesPlaneadas++</li>
 *   <li>{@code asignacion.completada} &rarr; clasesCompletadas++</li>
 *   <li>{@code asignacion.cancelada}  &rarr; clasesCanceladas++</li>
 * </ul>
 *
 * <p>Separado de {@link EstudianteAsignacionesEventListener} (que se ocupa
 * solo de transiciones de estado del estudiante) para tener single
 * responsibility y para que un fallo aqui no bloquee las transiciones.</p>
 *
 * <p><b>Idempotencia:</b> NO usamos {@code IdempotencyStore} aqui porque la
 * tabla {@code processed_events} tiene UNIQUE(event_id) y el listener viejo
 * (EstudianteAsignacionesEventListener) ya marca el mismo eventId al procesar
 * {@code asignacion.creada}. RabbitMQ entrega cada evento exactamente una vez
 * en condiciones normales (at-least-once con auto-ack en exito), por lo que
 * los contadores son confiables en el caso comun. Para el caso de reentrega
 * tras failover se asume la duplicacion ocasional como aceptable. El fix
 * definitivo seria cambiar el schema a UNIQUE(event_id, consumer_scope).</p>
 */
@Component
@Profile("!test")
@RabbitListener(queues = RabbitConfig.FROM_ASIGNACIONES_PROGRESO_QUEUE_NAME)
public class EstudianteProgresoAcademicoListener {

    private static final Logger log = LoggerFactory.getLogger(EstudianteProgresoAcademicoListener.class);

    private final ProgresoAcademicoService service;

    public EstudianteProgresoAcademicoListener(ProgresoAcademicoService service) {
        this.service = service;
    }

    @RabbitHandler
    public void onCreada(AsignacionCreadaEvent event) {
        if (event.getEstudianteId() == null) {
            log.warn("AsignacionCreadaEvent sin estudianteId, skip");
            return;
        }
        service.incrementarPlaneadas(event.getEstudianteId());
    }

    @RabbitHandler
    public void onCompletada(AsignacionCompletadaEvent event) {
        if (event.getEstudianteId() == null) {
            log.warn("AsignacionCompletadaEvent sin estudianteId, skip");
            return;
        }
        service.incrementarCompletadas(event.getEstudianteId());
    }

    @RabbitHandler
    public void onCancelada(AsignacionCanceladaEvent event) {
        if (event.getEstudianteId() == null) {
            log.warn("AsignacionCanceladaEvent sin estudianteId, skip");
            return;
        }
        service.incrementarCanceladas(event.getEstudianteId());
    }

    @RabbitHandler(isDefault = true)
    public void onMensajeDesconocido(Object payload) {
        log.warn("Mensaje desconocido en {}: {}",
                RabbitConfig.FROM_ASIGNACIONES_PROGRESO_QUEUE_NAME, payload);
    }
}
