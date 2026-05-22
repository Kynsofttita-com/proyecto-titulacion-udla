package com.escuela.estudiantes.listener;

import com.escuela.common.events.auth.UsuarioCreadoEvent;
import com.escuela.common.events.idempotency.IdempotencyStore;
import com.escuela.common.events.listener.AbstractEventListener;
import com.escuela.estudiantes.config.RabbitConfig;
import com.escuela.estudiantes.entity.Estudiante;
import com.escuela.estudiantes.repository.EstudianteRepository;
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
 * Estudiante correspondiente por email (matching).
 */
@Component
@Profile("!test")
@RabbitListener(queues = RabbitConfig.FROM_AUTH_QUEUE_NAME)
public class EstudianteAuthEventListener {

    private static final Logger log = LoggerFactory.getLogger(EstudianteAuthEventListener.class);
    private static final String MICROSERVICIO = "ms-estudiantes";

    private final EstudianteRepository repository;
    private final UsuarioCreadoHandler handler;

    public EstudianteAuthEventListener(EstudianteRepository repository,
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

            Optional<Estudiante> opt = repository.findByEmailAndDeletedAtIsNull(email);
            if (opt.isEmpty()) {
                log.debug("UsuarioCreadoEvent email={} no matchea estudiante existente", email);
                return;
            }
            Estudiante e = opt.get();
            if (e.getUsuarioId() != null && e.getUsuarioId().equals(usuarioId)) {
                log.debug("Estudiante id={} ya tiene usuario_id={}, skip", e.getId(), usuarioId);
                return;
            }
            e.setUsuarioId(usuarioId);
            repository.save(e);
            log.info("Estudiante id={} enlazado a usuario_id={} (email={})",
                    e.getId(), usuarioId, email);
        }
    }
}
