package com.escuela.estudiantes.listener;

import com.escuela.common.events.asignaciones.AsignacionCreadaEvent;
import com.escuela.common.events.idempotency.IdempotencyStore;
import com.escuela.common.events.listener.AbstractEventListener;
import com.escuela.estudiantes.config.RabbitConfig;
import com.escuela.estudiantes.service.EstudianteEstadoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Consume {@code asignacion.creada} de ms-asignaciones y dispara
 * {@link EstudianteEstadoService#procesarAsignacionCreada(Long)} para
 * la auto-transicion {@code MATRICULADO &rarr; CURSANDO} en el primer
 * registro de clase.
 *
 * <p>El bloqueo "no se puede asignar a PRE_MATRICULADO" se aplica en
 * ms-asignaciones antes de crear la asignacion, no aqui.</p>
 */
@Component
@Profile("!test")
@RabbitListener(queues = RabbitConfig.FROM_ASIGNACIONES_QUEUE_NAME)
public class EstudianteAsignacionesEventListener {

    private static final Logger log = LoggerFactory.getLogger(EstudianteAsignacionesEventListener.class);
    private static final String MICROSERVICIO = "ms-estudiantes";

    private final AsignacionCreadaHandler handler;

    public EstudianteAsignacionesEventListener(EstudianteEstadoService estadoService,
                                               IdempotencyStore idempotencyStore) {
        this.handler = new AsignacionCreadaHandler(estadoService, idempotencyStore);
    }

    @RabbitHandler
    public void onAsignacionCreada(AsignacionCreadaEvent event) {
        handler.process(event);
    }

    @RabbitHandler(isDefault = true)
    public void onMensajeDesconocido(Object payload) {
        log.warn("Mensaje desconocido en {}: {}", RabbitConfig.FROM_ASIGNACIONES_QUEUE_NAME, payload);
    }

    // -----------------------------------------------------------------------

    private static class AsignacionCreadaHandler extends AbstractEventListener<AsignacionCreadaEvent> {

        private final EstudianteEstadoService estadoService;

        AsignacionCreadaHandler(EstudianteEstadoService estadoService, IdempotencyStore store) {
            super(store, MICROSERVICIO);
            this.estadoService = estadoService;
        }

        public void process(AsignacionCreadaEvent event) {
            processWithIdempotency(event, AsignacionCreadaEvent.ROUTING_KEY);
        }

        @Override
        protected void handle(AsignacionCreadaEvent event) {
            if (event.getEstudianteId() == null) {
                log.warn("AsignacionCreadaEvent sin estudianteId, skip");
                return;
            }
            estadoService.procesarAsignacionCreada(event.getEstudianteId());
        }
    }
}
