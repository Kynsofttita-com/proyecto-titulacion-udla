package com.escuela.estudiantes.config;

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
 * Configuracion de RabbitMQ para MS-Estudiantes.
 *
 * Topologia:
 *   estudiantes.exchange (topic, durable)
 *     └─ binding "estudiantes.#" ──► estudiantes.queue ──(on failure)──► estudiantes.dlx ──► estudiantes.dlq
 */
@Configuration
public class RabbitConfig extends AbstractRabbitConfig {

    public static final String EXCHANGE_NAME = "estudiantes.exchange";
    public static final String QUEUE_NAME = "estudiantes.queue";
    public static final String DLX_NAME = "estudiantes.dlx";
    public static final String DLQ_NAME = "estudiantes.dlq";
    public static final String ROUTING_KEY = "estudiantes.#";

    @Bean
    public TopicExchange estudiantesExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue estudiantesQueue() {
        return QueueBuilder.durable(QUEUE_NAME)
                .withArgument("x-dead-letter-exchange", DLX_NAME)
                .build();
    }

    @Bean
    public DirectExchange estudiantesDlx() {
        return new DirectExchange(DLX_NAME, true, false);
    }

    @Bean
    public Queue estudiantesDlq() {
        return QueueBuilder.durable(DLQ_NAME).build();
    }

    @Bean
    public Binding estudiantesQueueBinding() {
        return BindingBuilder.bind(estudiantesQueue()).to(estudiantesExchange()).with(ROUTING_KEY);
    }

    @Bean
    public Binding estudiantesDlqBinding() {
        return BindingBuilder.bind(estudiantesDlq()).to(estudiantesDlx()).with("");
    }
}
