package com.escuela.auth.service;

import com.escuela.auth.config.RabbitConfig;
import com.escuela.common.events.auth.UsuarioCreadoEvent;
import com.escuela.common.events.publisher.EventPublisher;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Servicio que publica eventos de dominio de MS-Auth a RabbitMQ.
 *
 * <p>Construye un {@link EventPublisher} a partir del {@link RabbitTemplate}
 * provisto por {@code AbstractRabbitConfig}. Asi se evita declarar el
 * EventPublisher como bean global (que tendria problemas de orden con
 * {@code @ConditionalOnBean} dentro de la misma @Configuration).</p>
 *
 * <p>Solo activo fuera del perfil "test" porque en tests RabbitTemplate no
 * existe (RabbitAutoConfiguration esta excluida).</p>
 */
@Service
@Profile("!test")
public class UsuarioEventPublisher {

    private final EventPublisher eventPublisher;

    public UsuarioEventPublisher(RabbitTemplate rabbitTemplate,
                                 @Value("${spring.application.name:ms-auth}") String applicationName) {
        this.eventPublisher = new EventPublisher(rabbitTemplate, applicationName);
    }

    public void publicarUsuarioCreado(UsuarioCreadoEvent event) {
        eventPublisher.publish(
                RabbitConfig.EXCHANGE_NAME,
                UsuarioCreadoEvent.ROUTING_KEY,
                event);
    }
}
