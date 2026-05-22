package com.escuela.instructores.listener;

import com.escuela.common.events.auth.UsuarioCreadoEvent;
import com.escuela.common.events.idempotency.IdempotencyStore;
import com.escuela.common.events.listener.AbstractEventListener;
import com.escuela.instructores.config.RabbitConfig;
import com.escuela.instructores.entity.Instructor;
import com.escuela.instructores.repository.InstructorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Consume {@code auth.usuario.creado} y enlaza el {@code usuario_id} al
 * Instructor correspondiente por email (matching).
 *
 * <p>Cuando MS-Auth crea un usuario tras recibir un {@code InstructorCreadoEvent},
 * publica este evento. Este listener cierra el loop actualizando el campo
 * {@code usuario_id} en la entidad Instructor.</p>
 */
@Component
@Profile("!test")
@RabbitListener(queues = RabbitConfig.FROM_AUTH_QUEUE_NAME)
public class InstructorAuthEventListener {

    private static final Logger log = LoggerFactory.getLogger(InstructorAuthEventListener.class);
    private static final String MICROSERVICIO = "ms-instructores";

    private final InstructorRepository repository;
    private final UsuarioCreadoHandler handler;

    public InstructorAuthEventListener(InstructorRepository repository,
                                       IdempotencyStore idempotencyStore) {
        this.repository = repository;
        this.handler = new UsuarioCreadoHandler(idempotencyStore);
    }

    @RabbitHandler
    public void onUsuarioCreado(UsuarioCreadoEvent event) {
        handler.process(event);
    }

    @RabbitHandler(isDefault = true)
    public void onMensajeDesconocido(Object payload) {
        log.warn("Mensaje desconocido en {}: {}", RabbitConfig.FROM_AUTH_QUEUE_NAME, payload);
    }

    // -----------------------------------------------------------------------

    private class UsuarioCreadoHandler extends AbstractEventListener<UsuarioCreadoEvent> {

        UsuarioCreadoHandler(IdempotencyStore store) {
            super(store, MICROSERVICIO);
        }

        public void process(UsuarioCreadoEvent event) {
            processWithIdempotency(event, UsuarioCreadoEvent.ROUTING_KEY);
        }

        @Override
        @Transactional
        protected void handle(UsuarioCreadoEvent event) {
            String email = event.getEmail();
            Long usuarioId = event.getUsuarioId();
            if (email == null || usuarioId == null) {
                log.warn("UsuarioCreadoEvent incompleto (email={} usuarioId={}), skip",
                        email, usuarioId);
                return;
            }

            Optional<Instructor> opt = repository.findByEmailAndDeletedAtIsNull(email);
            if (opt.isEmpty()) {
                // No es de este dominio (o se borro) — ignorar
                log.debug("UsuarioCreadoEvent email={} no matchea instructor existente", email);
                return;
            }
            Instructor i = opt.get();
            if (i.getUsuarioId() != null && i.getUsuarioId().equals(usuarioId)) {
                log.debug("Instructor id={} ya tiene usuario_id={}, skip", i.getId(), usuarioId);
                return;
            }
            i.setUsuarioId(usuarioId);
            repository.save(i);
            log.info("Instructor id={} enlazado a usuario_id={} (email={})",
                    i.getId(), usuarioId, email);
        }
    }
}
