package com.escuela.asignaciones.config;

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
 * Configuracion de RabbitMQ para MS-Asignaciones.
 *
 * Topologia:
 *   asignaciones.exchange (topic, durable)
 *     └─ binding "asignaciones.#" ──► asignaciones.queue ──(on failure)──► asignaciones.dlx ──► asignaciones.dlq
 */
@Configuration
public class RabbitConfig extends AbstractRabbitConfig {

    public static final String EXCHANGE_NAME = "asignaciones.exchange";
    public static final String QUEUE_NAME = "asignaciones.queue";
    public static final String DLX_NAME = "asignaciones.dlx";
    public static final String DLQ_NAME = "asignaciones.dlq";
    public static final String ROUTING_KEY = "asignaciones.#";

    @Bean
    public TopicExchange asignacionesExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue asignacionesQueue() {
        return QueueBuilder.durable(QUEUE_NAME)
                .withArgument("x-dead-letter-exchange", DLX_NAME)
                .build();
    }

    @Bean
    public DirectExchange asignacionesDlx() {
        return new DirectExchange(DLX_NAME, true, false);
    }

    @Bean
    public Queue asignacionesDlq() {
        return QueueBuilder.durable(DLQ_NAME).build();
    }

    @Bean
    public Binding asignacionesQueueBinding() {
        return BindingBuilder.bind(asignacionesQueue()).to(asignacionesExchange()).with(ROUTING_KEY);
    }

    @Bean
    public Binding asignacionesDlqBinding() {
        return BindingBuilder.bind(asignacionesDlq()).to(asignacionesDlx()).with("");
    }
}
