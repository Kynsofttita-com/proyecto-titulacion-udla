package com.escuela.reportes.config;

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
 * Configuracion de RabbitMQ para MS-Reportes.
 *
 * Topologia:
 *   reportes.exchange (topic, durable)
 *     └─ binding "reportes.#" ──► reportes.queue ──(on failure)──► reportes.dlx ──► reportes.dlq
 */
@Configuration
public class RabbitConfig extends AbstractRabbitConfig {

    public static final String EXCHANGE_NAME = "reportes.exchange";
    public static final String QUEUE_NAME = "reportes.queue";
    public static final String DLX_NAME = "reportes.dlx";
    public static final String DLQ_NAME = "reportes.dlq";
    public static final String ROUTING_KEY = "reportes.#";

    @Bean
    public TopicExchange reportesExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue reportesQueue() {
        return QueueBuilder.durable(QUEUE_NAME)
                .withArgument("x-dead-letter-exchange", DLX_NAME)
                .build();
    }

    @Bean
    public DirectExchange reportesDlx() {
        return new DirectExchange(DLX_NAME, true, false);
    }

    @Bean
    public Queue reportesDlq() {
        return QueueBuilder.durable(DLQ_NAME).build();
    }

    @Bean
    public Binding reportesQueueBinding() {
        return BindingBuilder.bind(reportesQueue()).to(reportesExchange()).with(ROUTING_KEY);
    }

    @Bean
    public Binding reportesDlqBinding() {
        return BindingBuilder.bind(reportesDlq()).to(reportesDlx()).with("");
    }
}
