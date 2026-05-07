package com.escuela.instructores.config;

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
 * Configuracion de RabbitMQ para MS-Instructores.
 *
 * Topologia:
 *   instructores.exchange (topic, durable)
 *     └─ binding "instructores.#" ──► instructores.queue ──(on failure)──► instructores.dlx ──► instructores.dlq
 */
@Configuration
public class RabbitConfig extends AbstractRabbitConfig {

    public static final String EXCHANGE_NAME = "instructores.exchange";
    public static final String QUEUE_NAME = "instructores.queue";
    public static final String DLX_NAME = "instructores.dlx";
    public static final String DLQ_NAME = "instructores.dlq";
    public static final String ROUTING_KEY = "instructores.#";

    @Bean
    public TopicExchange instructoresExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue instructoresQueue() {
        return QueueBuilder.durable(QUEUE_NAME)
                .withArgument("x-dead-letter-exchange", DLX_NAME)
                .build();
    }

    @Bean
    public DirectExchange instructoresDlx() {
        return new DirectExchange(DLX_NAME, true, false);
    }

    @Bean
    public Queue instructoresDlq() {
        return QueueBuilder.durable(DLQ_NAME).build();
    }

    @Bean
    public Binding instructoresQueueBinding() {
        return BindingBuilder.bind(instructoresQueue()).to(instructoresExchange()).with(ROUTING_KEY);
    }

    @Bean
    public Binding instructoresDlqBinding() {
        return BindingBuilder.bind(instructoresDlq()).to(instructoresDlx()).with("");
    }
}
