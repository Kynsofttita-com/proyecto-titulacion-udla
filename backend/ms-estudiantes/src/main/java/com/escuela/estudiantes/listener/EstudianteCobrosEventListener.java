package com.escuela.estudiantes.listener;

import com.escuela.common.events.cobros.PagoRegistradoEvent;
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
 * Consume {@code pago.registrado} desde {@code cobros.exchange} y dispara la
 * transicion academica + financiera del estudiante via
 * {@link EstudianteEstadoService#procesarPagoRegistrado(Long, String)}.
 *
 * <p>Idempotente: si el {@code eventId} ya fue procesado se descarta. Si la
 * transicion no aplica para el estado actual del estudiante, el service hace
 * skip silencioso.</p>
 */
@Component
@Profile("!test")
@RabbitListener(queues = RabbitConfig.FROM_COBROS_QUEUE_NAME)
public class EstudianteCobrosEventListener {

    private static final Logger log = LoggerFactory.getLogger(EstudianteCobrosEventListener.class);
    private static final String MICROSERVICIO = "ms-estudiantes";

    private final PagoRegistradoHandler handler;

    public EstudianteCobrosEventListener(EstudianteEstadoService estadoService,
                                         IdempotencyStore idempotencyStore) {
        this.handler = new PagoRegistradoHandler(estadoService, idempotencyStore);
    }

    @RabbitHandler
    public void onPagoRegistrado(PagoRegistradoEvent event) {
        handler.process(event);
    }

    @RabbitHandler(isDefault = true)
    public void onMensajeDesconocido(Object payload) {
        log.warn("Mensaje desconocido en {}: {}", RabbitConfig.FROM_COBROS_QUEUE_NAME, payload);
    }

    // -----------------------------------------------------------------------

    private static class PagoRegistradoHandler extends AbstractEventListener<PagoRegistradoEvent> {

        private final EstudianteEstadoService estadoService;

        PagoRegistradoHandler(EstudianteEstadoService estadoService, IdempotencyStore store) {
            super(store, MICROSERVICIO);
            this.estadoService = estadoService;
        }

        public void process(PagoRegistradoEvent event) {
            processWithIdempotency(event, PagoRegistradoEvent.ROUTING_KEY);
        }

        @Override
        protected void handle(PagoRegistradoEvent event) {
            if (event.getEstudianteId() == null) {
                log.warn("PagoRegistradoEvent sin estudianteId, skip");
                return;
            }
            estadoService.procesarPagoRegistrado(event.getEstudianteId(), event.getEstadoFactura());
        }
    }
}
