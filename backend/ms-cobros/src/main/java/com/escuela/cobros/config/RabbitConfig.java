package com.escuela.cobros.config;

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
 * Configuracion de RabbitMQ para MS-Cobros.
 *
 * Topologia:
 *   cobros.exchange (topic, durable)
 *     └─ binding "cobros.#" ──► cobros.queue ──(on failure)──► cobros.dlx ──► cobros.dlq
 */
@Configuration
public class RabbitConfig extends AbstractRabbitConfig {

    public static final String EXCHANGE_NAME = "cobros.exchange";
    public static final String QUEUE_NAME = "cobros.queue";
    public static final String DLX_NAME = "cobros.dlx";
    public static final String DLQ_NAME = "cobros.dlq";
    public static final String ROUTING_KEY = "cobros.#";

    @Bean
    public TopicExchange cobrosExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue cobrosQueue() {
        return QueueBuilder.durable(QUEUE_NAME)
                .withArgument("x-dead-letter-exchange", DLX_NAME)
                .build();
    }

    @Bean
    public DirectExchange cobrosDlx() {
        return new DirectExchange(DLX_NAME, true, false);
    }

    @Bean
    public Queue cobrosDlq() {
        return QueueBuilder.durable(DLQ_NAME).build();
    }

    @Bean
    public Binding cobrosQueueBinding() {
        return BindingBuilder.bind(cobrosQueue()).to(cobrosExchange()).with(ROUTING_KEY);
    }

    @Bean
    public Binding cobrosDlqBinding() {
        return BindingBuilder.bind(cobrosDlq()).to(cobrosDlx()).with("");
    }
}
