package com.escuela.auth.listener;

import com.escuela.auth.config.RabbitConfig;
import com.escuela.auth.service.AutoUsuarioCreator;
import com.escuela.auth.service.UsuarioService;
import com.escuela.common.events.estudiantes.EstudianteCreadoEvent;
import com.escuela.common.events.estudiantes.EstudianteEliminadoEvent;
import com.escuela.common.events.idempotency.IdempotencyStore;
import com.escuela.common.events.instructores.InstructorCreadoEvent;
import com.escuela.common.events.instructores.InstructorEliminadoEvent;
import com.escuela.common.events.listener.AbstractEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Listener de MS-Auth que consume eventos de dominio (Estudiantes, Instructores)
 * y crea automaticamente el Usuario correspondiente.
 *
 * <p>Tras crear el usuario, publica {@link com.escuela.common.events.auth.UsuarioCreadoEvent}
 * para que los MS de origen enlacen el {@code usuario_id} en su entidad.</p>
 *
 * <p>Idempotente via {@link AbstractEventListener#processWithIdempotency}: si
 * el mismo eventId se recibe dos veces, solo se procesa una.</p>
 */
@Component
@Profile("!test")
@RabbitListener(queues = RabbitConfig.FROM_DOMAINS_QUEUE_NAME)
public class DomainEventListener {

    private static final Logger log = LoggerFactory.getLogger(DomainEventListener.class);
    private static final String MICROSERVICIO = "ms-auth";

    private final AutoUsuarioCreator autoCreator;
    private final UsuarioService usuarioService;
    private final EstudianteCreadoHandler estudianteHandler;
    private final InstructorCreadoHandler instructorHandler;
    private final EstudianteEliminadoHandler estudianteEliminadoHandler;
    private final InstructorEliminadoHandler instructorEliminadoHandler;

    public DomainEventListener(AutoUsuarioCreator autoCreator,
                               UsuarioService usuarioService,
                               IdempotencyStore idempotencyStore) {
        this.autoCreator = autoCreator;
        this.usuarioService = usuarioService;
        this.estudianteHandler = new EstudianteCreadoHandler(idempotencyStore);
        this.instructorHandler = new InstructorCreadoHandler(idempotencyStore);
        this.estudianteEliminadoHandler = new EstudianteEliminadoHandler(idempotencyStore);
        this.instructorEliminadoHandler = new InstructorEliminadoHandler(idempotencyStore);
    }

    @RabbitHandler
    public void onEstudianteCreado(EstudianteCreadoEvent event) {
        estudianteHandler.process(event);
    }

    @RabbitHandler
    public void onInstructorCreado(InstructorCreadoEvent event) {
        instructorHandler.process(event);
    }

    @RabbitHandler
    public void onEstudianteEliminado(EstudianteEliminadoEvent event) {
        estudianteEliminadoHandler.process(event);
    }

    @RabbitHandler
    public void onInstructorEliminado(InstructorEliminadoEvent event) {
        instructorEliminadoHandler.process(event);
    }

    @RabbitHandler(isDefault = true)
    public void onMensajeDesconocido(Object payload) {
        log.warn("Mensaje desconocido en {}: {}", RabbitConfig.FROM_DOMAINS_QUEUE_NAME, payload);
    }

    // -----------------------------------------------------------------------

    private class EstudianteCreadoHandler extends AbstractEventListener<EstudianteCreadoEvent> {
        EstudianteCreadoHandler(IdempotencyStore store) {
            super(store, MICROSERVICIO);
        }

        public void process(EstudianteCreadoEvent event) {
            processWithIdempotency(event, EstudianteCreadoEvent.ROUTING_KEY);
        }

        @Override
        protected void handle(EstudianteCreadoEvent event) {
            String[] partes = splitNombreCompleto(event.getNombreCompleto());
            autoCreator.crearYNotificar(event.getEmail(), partes[0], partes[1], "ESTUDIANTE");
        }
    }

    private class InstructorCreadoHandler extends AbstractEventListener<InstructorCreadoEvent> {
        InstructorCreadoHandler(IdempotencyStore store) {
            super(store, MICROSERVICIO);
        }

        public void process(InstructorCreadoEvent event) {
            processWithIdempotency(event, InstructorCreadoEvent.ROUTING_KEY);
        }

        @Override
        protected void handle(InstructorCreadoEvent event) {
            autoCreator.crearYNotificar(event.getEmail(),
                    event.getNombre(), event.getApellido(), "INSTRUCTOR");
        }
    }

    private class EstudianteEliminadoHandler extends AbstractEventListener<EstudianteEliminadoEvent> {
        EstudianteEliminadoHandler(IdempotencyStore store) {
            super(store, MICROSERVICIO);
        }

        public void process(EstudianteEliminadoEvent event) {
            processWithIdempotency(event, EstudianteEliminadoEvent.ROUTING_KEY);
        }

        @Override
        protected void handle(EstudianteEliminadoEvent event) {
            usuarioService.desactivarPorCedula(event.getCedula(), "ESTUDIANTE");
        }
    }

    private class InstructorEliminadoHandler extends AbstractEventListener<InstructorEliminadoEvent> {
        InstructorEliminadoHandler(IdempotencyStore store) {
            super(store, MICROSERVICIO);
        }

        public void process(InstructorEliminadoEvent event) {
            processWithIdempotency(event, InstructorEliminadoEvent.ROUTING_KEY);
        }

        @Override
        protected void handle(InstructorEliminadoEvent event) {
            usuarioService.desactivarPorCedula(event.getCedula(), "INSTRUCTOR");
        }
    }

    /** Divide "Juan Perez" en {"Juan", "Perez"}. Si solo hay 1 token, apellido es "(sin definir)". */
    private static String[] splitNombreCompleto(String nombreCompleto) {
        if (nombreCompleto == null || nombreCompleto.isBlank()) {
            return new String[]{"(sin definir)", "(sin definir)"};
        }
        String[] tokens = nombreCompleto.trim().split("\\s+", 2);
        if (tokens.length == 1) {
            return new String[]{tokens[0], "(sin definir)"};
        }
        return tokens;
    }
}
