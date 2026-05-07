package com.escuela.notificaciones.listener;

import com.escuela.common.events.auth.UsuarioCreadoEvent;
import com.escuela.common.events.idempotency.IdempotencyStore;
import com.escuela.common.events.listener.AbstractEventListener;
import com.escuela.notificaciones.config.RabbitConfig;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Consumer de {@link UsuarioCreadoEvent} en MS-Notificaciones.
 *
 * <p>Escucha {@code notificaciones.queue} (que tiene un binding adicional al
 * exchange {@code auth.exchange} con routing key {@code auth.usuario.creado},
 * ver {@link RabbitConfig#authUsuarioCreadoBinding}).</p>
 *
 * <p>En T3.3 solo loggea el evento. En sprints siguientes aqui se enviara el
 * email de bienvenida y se crearan las preferencias de notificacion por
 * defecto del usuario.</p>
 */
@Component
@Profile("!test")
@RabbitListener(queues = RabbitConfig.QUEUE_NAME)
public class UsuarioCreadoListener extends AbstractEventListener<UsuarioCreadoEvent> {

    private static final String MICROSERVICIO = "ms-notificaciones";

    public UsuarioCreadoListener(IdempotencyStore idempotencyStore) {
        super(idempotencyStore, MICROSERVICIO);
    }

    @RabbitHandler
    public void onUsuarioCreado(UsuarioCreadoEvent event) {
        processWithIdempotency(event, UsuarioCreadoEvent.ROUTING_KEY);
    }

    @RabbitHandler(isDefault = true)
    public void onMensajeDesconocido(Object payload) {
        log.warn("Mensaje desconocido recibido en {}: {}",
                RabbitConfig.QUEUE_NAME, payload);
    }

    @Override
    protected void handle(UsuarioCreadoEvent event) {
        log.info("UsuarioCreadoEvent procesado: usuarioId={}, email={}, source={}, eventId={}",
                event.getUsuarioId(),
                event.getEmail(),
                event.getSource(),
                event.getEventId());
        // TODO Sprint 4+: enviar email de bienvenida + crear preferencias_notificacion
    }
}
