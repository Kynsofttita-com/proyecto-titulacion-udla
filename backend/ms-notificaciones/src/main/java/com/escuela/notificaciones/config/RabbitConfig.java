package com.escuela.notificaciones.config;

import com.escuela.common.events.config.AbstractRabbitConfig;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracion de RabbitMQ para MS-Notificaciones.
 *
 * Topologia:
 *   notificaciones.exchange (topic, durable)
 *     └─ binding "notificaciones.#" ──► notificaciones.queue ──(on failure)──► notificaciones.dlx ──► notificaciones.dlq
 */
@Configuration
public class RabbitConfig extends AbstractRabbitConfig {

    public static final String EXCHANGE_NAME = "notificaciones.exchange";
    public static final String QUEUE_NAME = "notificaciones.queue";
    public static final String DLX_NAME = "notificaciones.dlx";
    public static final String DLQ_NAME = "notificaciones.dlq";
    public static final String ROUTING_KEY = "notificaciones.#";

    @Bean
    public TopicExchange notificacionesExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue notificacionesQueue() {
        return QueueBuilder.durable(QUEUE_NAME)
                .withArgument("x-dead-letter-exchange", DLX_NAME)
                .build();
    }

    @Bean
    public DirectExchange notificacionesDlx() {
        return new DirectExchange(DLX_NAME, true, false);
    }

    @Bean
    public Queue notificacionesDlq() {
        return QueueBuilder.durable(DLQ_NAME).build();
    }

    @Bean
    public Binding notificacionesQueueBinding() {
        return BindingBuilder.bind(notificacionesQueue()).to(notificacionesExchange()).with(ROUTING_KEY);
    }

    @Bean
    public Binding notificacionesDlqBinding() {
        return BindingBuilder.bind(notificacionesDlq()).to(notificacionesDlx()).with("");
    }
}
