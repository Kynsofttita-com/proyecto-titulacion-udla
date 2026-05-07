package com.escuela.notificaciones.listener;

import com.escuela.common.events.auth.PasswordResetSolicitadoEvent;
import com.escuela.common.events.auth.UsuarioBloqueadoEvent;
import com.escuela.common.events.auth.UsuarioCreadoEvent;
import com.escuela.common.events.idempotency.IdempotencyStore;
import com.escuela.common.events.listener.AbstractEventListener;
import com.escuela.notificaciones.config.NotificacionesProperties;
import com.escuela.notificaciones.config.RabbitConfig;
import com.escuela.notificaciones.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Listener multiplexer de eventos del dominio auth en MS-Notificaciones.
 *
 * <p>Una sola clase con varios {@code @RabbitHandler} es la forma correcta de
 * consumir multiples tipos de eventos sobre la misma queue: si tuvieramos
 * varias clases con {@code @RabbitListener(queues = "notificaciones.queue")},
 * Spring AMQP las trataria como consumers separados y los mensajes se
 * distribuirian round-robin entre ellos (incorrecto).</p>
 *
 * <p>Cada {@code @RabbitHandler} delega a un metodo {@code handleX()} que
 * pasa por {@link AbstractEventListener#processWithIdempotency} para garantizar
 * que cada {@code eventId} se procese una sola vez.</p>
 */
@Component
@Profile("!test")
@RabbitListener(queues = RabbitConfig.QUEUE_NAME)
public class NotificacionesAuthEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificacionesAuthEventListener.class);
    private static final String MICROSERVICIO = "ms-notificaciones";
    private static final DateTimeFormatter LOCK_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault());

    private final EmailService emailService;
    private final NotificacionesProperties properties;

    /** Helpers internos por tipo de evento que reusan la idempotencia comun. */
    private final UsuarioCreadoHandler usuarioCreadoHandler;
    private final PasswordResetSolicitadoHandler passwordResetHandler;
    private final UsuarioBloqueadoHandler usuarioBloqueadoHandler;

    public NotificacionesAuthEventListener(IdempotencyStore idempotencyStore,
                                           EmailService emailService,
                                           NotificacionesProperties properties) {
        this.emailService = emailService;
        this.properties = properties;
        this.usuarioCreadoHandler = new UsuarioCreadoHandler(idempotencyStore);
        this.passwordResetHandler = new PasswordResetSolicitadoHandler(idempotencyStore);
        this.usuarioBloqueadoHandler = new UsuarioBloqueadoHandler(idempotencyStore);
    }

    @RabbitHandler
    public void onUsuarioCreado(UsuarioCreadoEvent event) {
        usuarioCreadoHandler.process(event);
    }

    @RabbitHandler
    public void onPasswordResetSolicitado(PasswordResetSolicitadoEvent event) {
        passwordResetHandler.process(event);
    }

    @RabbitHandler
    public void onUsuarioBloqueado(UsuarioBloqueadoEvent event) {
        usuarioBloqueadoHandler.process(event);
    }

    @RabbitHandler(isDefault = true)
    public void onMensajeDesconocido(Object payload) {
        log.warn("Mensaje desconocido recibido en {}: {}", RabbitConfig.QUEUE_NAME, payload);
    }

    // -----------------------------------------------------------------------
    // Handlers internos (clases anidadas para reusar AbstractEventListener)
    // -----------------------------------------------------------------------

    private class UsuarioCreadoHandler extends AbstractEventListener<UsuarioCreadoEvent> {
        UsuarioCreadoHandler(IdempotencyStore store) { super(store, MICROSERVICIO); }

        public void process(UsuarioCreadoEvent event) {
            processWithIdempotency(event, UsuarioCreadoEvent.ROUTING_KEY);
        }

        @Override
        protected void handle(UsuarioCreadoEvent event) {
            log.info("UsuarioCreadoEvent procesado: usuarioId={}, email={}, source={}, eventId={}",
                    event.getUsuarioId(), event.getEmail(), event.getSource(), event.getEventId());
            // TODO Sprint 5+: enviar email de bienvenida + crear preferencias_notificacion
        }
    }

    private class PasswordResetSolicitadoHandler extends AbstractEventListener<PasswordResetSolicitadoEvent> {
        PasswordResetSolicitadoHandler(IdempotencyStore store) { super(store, MICROSERVICIO); }

        public void process(PasswordResetSolicitadoEvent event) {
            processWithIdempotency(event, PasswordResetSolicitadoEvent.ROUTING_KEY);
        }

        @Override
        protected void handle(PasswordResetSolicitadoEvent event) {
            log.info("PasswordResetSolicitadoEvent procesado: usuarioId={}, eventId={}",
                    event.getUsuarioId(), event.getEventId());

            String link = String.format("%s/reset-password?token=%s",
                    properties.getFrontendBaseUrl(), event.getResetToken());

            Map<String, Object> vars = new HashMap<>();
            vars.put("nombre", event.getNombre());
            vars.put("link", link);
            vars.put("minutos", event.getExpiraEnMinutos());

            emailService.enviar(
                    event.getEmail(),
                    "Recuperar contrasenia - Escuela de Conduccion",
                    "RECUPERAR_PASSWORD",
                    "emails/password-reset",
                    vars);
        }
    }

    private class UsuarioBloqueadoHandler extends AbstractEventListener<UsuarioBloqueadoEvent> {
        UsuarioBloqueadoHandler(IdempotencyStore store) { super(store, MICROSERVICIO); }

        public void process(UsuarioBloqueadoEvent event) {
            processWithIdempotency(event, UsuarioBloqueadoEvent.ROUTING_KEY);
        }

        @Override
        protected void handle(UsuarioBloqueadoEvent event) {
            log.info("UsuarioBloqueadoEvent procesado: usuarioId={}, lockUntil={}, eventId={}",
                    event.getUsuarioId(), event.getLockUntil(), event.getEventId());

            String lockUntilTexto = event.getLockUntil() != null
                    ? LOCK_FORMATTER.format(event.getLockUntil())
                    : "en pocos minutos";

            Map<String, Object> vars = new HashMap<>();
            vars.put("nombre", event.getNombre());
            vars.put("intentos", event.getIntentosFallidos());
            vars.put("lockUntilTexto", lockUntilTexto);

            emailService.enviar(
                    event.getEmail(),
                    "Cuenta bloqueada temporalmente - Escuela de Conduccion",
                    "CUENTA_BLOQUEADA",
                    "emails/usuario-bloqueado",
                    vars);
        }
    }
}
