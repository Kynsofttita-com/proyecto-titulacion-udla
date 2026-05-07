package com.escuela.auth.service;

import com.escuela.auth.config.RabbitConfig;
import com.escuela.common.events.BaseEvent;
import com.escuela.common.events.auth.PasswordResetSolicitadoEvent;
import com.escuela.common.events.auth.UsuarioBloqueadoEvent;
import com.escuela.common.events.auth.UsuarioCreadoEvent;
import com.escuela.common.events.publisher.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Wrapper que publica eventos de dominio de MS-Auth de forma resiliente.
 *
 * <p>Construye el {@link EventPublisher} a partir del {@link RabbitTemplate}
 * inyectado de forma opcional ({@code ObjectProvider}). En runtime con RabbitMQ,
 * el RabbitTemplate existe (creado por {@code AbstractRabbitConfig}) → se
 * arma el EventPublisher al primer uso (lazy, cached). En tests con
 * {@code RabbitAutoConfiguration} excluida, no hay RabbitTemplate → los publish
 * se vuelven no-ops (con log warning).</p>
 */
@Component
public class AuthEventDispatcher {

    private static final Logger log = LoggerFactory.getLogger(AuthEventDispatcher.class);

    private final ObjectProvider<RabbitTemplate> rabbitTemplateProvider;
    private final String applicationName;
    private volatile EventPublisher cached;

    public AuthEventDispatcher(ObjectProvider<RabbitTemplate> rabbitTemplateProvider,
                               @Value("${spring.application.name:ms-auth}") String applicationName) {
        this.rabbitTemplateProvider = rabbitTemplateProvider;
        this.applicationName = applicationName;
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

    private void publish(String routingKey, BaseEvent event) {
        EventPublisher publisher = resolvePublisher();
        if (publisher == null) {
            log.warn("RabbitTemplate no disponible (probablemente perfil test); " +
                    "skip evento routingKey={} payload={}", routingKey, event);
            return;
        }
        publisher.publish(RabbitConfig.EXCHANGE_NAME, routingKey, event);
    }

    /**
     * Construye el publisher la primera vez que se invoca, despues lo cachea.
     * Es lazy porque el RabbitTemplate puede no estar disponible en el
     * constructor (Spring crea AuthEventDispatcher antes que RabbitTemplate
     * en algunos ordenes de inicializacion).
     */
    private EventPublisher resolvePublisher() {
        if (cached != null) return cached;
        RabbitTemplate template = rabbitTemplateProvider.getIfAvailable();
        if (template == null) return null;
        synchronized (this) {
            if (cached == null) {
                cached = new EventPublisher(template, applicationName);
            }
        }
        return cached;
    }
}
