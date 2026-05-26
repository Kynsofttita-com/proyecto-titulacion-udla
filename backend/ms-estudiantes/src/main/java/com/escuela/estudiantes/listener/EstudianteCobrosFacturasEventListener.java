package com.escuela.estudiantes.listener;

import com.escuela.common.events.cobros.FacturaEmitidaEvent;
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
 * Consume {@code factura.emitida} de ms-cobros y dispara
 * {@link EstudianteEstadoService#procesarFacturaEmitida(Long, String)} para:
 * <ul>
 *   <li>Actualizar {@code situacion_pago}: CONTADO &rarr; PENDIENTE_PAGO,
 *       CREDITO &rarr; PAGADO_TOTAL.</li>
 *   <li>Auto-transicion {@code PRE_MATRICULADO &rarr; MATRICULADO} si CREDITO
 *       (el estudiante puede iniciar clases inmediatamente porque se asume
 *       cobro automatico por tarjeta).</li>
 * </ul>
 */
@Component
@Profile("!test")
@RabbitListener(queues = RabbitConfig.FROM_COBROS_FACTURAS_QUEUE_NAME)
public class EstudianteCobrosFacturasEventListener {

    private static final Logger log = LoggerFactory.getLogger(EstudianteCobrosFacturasEventListener.class);
    private static final String MICROSERVICIO = "ms-estudiantes";

    private final FacturaEmitidaHandler handler;

    public EstudianteCobrosFacturasEventListener(EstudianteEstadoService estadoService,
                                                 IdempotencyStore idempotencyStore) {
        this.handler = new FacturaEmitidaHandler(estadoService, idempotencyStore);
    }

    @RabbitHandler
    public void onFacturaEmitida(FacturaEmitidaEvent event) {
        handler.process(event);
    }

    @RabbitHandler(isDefault = true)
    public void onMensajeDesconocido(Object payload) {
        log.warn("Mensaje desconocido en {}: {}", RabbitConfig.FROM_COBROS_FACTURAS_QUEUE_NAME, payload);
    }

    // -----------------------------------------------------------------------

    private static class FacturaEmitidaHandler extends AbstractEventListener<FacturaEmitidaEvent> {

        private final EstudianteEstadoService estadoService;

        FacturaEmitidaHandler(EstudianteEstadoService estadoService, IdempotencyStore store) {
            super(store, MICROSERVICIO);
            this.estadoService = estadoService;
        }

        public void process(FacturaEmitidaEvent event) {
            processWithIdempotency(event, FacturaEmitidaEvent.ROUTING_KEY);
        }

        @Override
        protected void handle(FacturaEmitidaEvent event) {
            if (event.getEstudianteId() == null) {
                log.warn("FacturaEmitidaEvent sin estudianteId, skip (facturaId={})", event.getFacturaId());
                return;
            }
            estadoService.procesarFacturaEmitida(event.getEstudianteId(), event.getTipoPago());
        }
    }
}
