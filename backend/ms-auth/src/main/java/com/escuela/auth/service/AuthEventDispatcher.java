package com.escuela.auth.service;

import com.escuela.auth.config.RabbitConfig;
import com.escuela.common.events.auth.PasswordResetSolicitadoEvent;
import com.escuela.common.events.auth.UsuarioBloqueadoEvent;
import com.escuela.common.events.auth.UsuarioCreadoEvent;
import com.escuela.common.events.publisher.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Wrapper que publica eventos de dominio de MS-Auth de forma resiliente.
 *
 * <p>Usa {@link ObjectProvider} para obtener el {@link EventPublisher} de
 * forma opcional: en runtime con RabbitMQ existe; en tests con
 * RabbitAutoConfiguration excluida no existe y los publish se vuelven
 * no-ops (con log warning), para que la lógica de negocio del AuthService
 * sea testeable sin Mockito de RabbitMQ.</p>
 */
@Component
public class AuthEventDispatcher {

    private static final Logger log = LoggerFactory.getLogger(AuthEventDispatcher.class);

    private final ObjectProvider<EventPublisher> publisherProvider;

    public AuthEventDispatcher(ObjectProvider<EventPublisher> publisherProvider) {
        this.publisherProvider = publisherProvider;
    }

    public void publishUsuarioCreado(UsuarioCreadoEvent event) {
        publish(UsuarioCreadoEvent.ROUTING_KEY, event);
    }

    public void publishUsuarioBloqueado(UsuarioBloqueadoEvent event) {
        publish(UsuarioBloqueadoEvent.ROUTING_KEY, event);
    }

    public void publishPasswordResetSolicitado(PasswordResetSolicitadoEvent event) {
        publish(PasswordResetSolicitadoEvent.ROUTING_KEY, event);
    }

    private void publish(String routingKey, Object event) {
        EventPublisher publisher = publisherProvider.getIfAvailable();
        if (publisher == null) {
            log.warn("EventPublisher no disponible (probablemente perfil test); " +
                    "skip evento routingKey={} payload={}", routingKey, event);
            return;
        }
        publisher.publish(RabbitConfig.EXCHANGE_NAME, routingKey,
                (com.escuela.common.events.BaseEvent) event);
    }
}
