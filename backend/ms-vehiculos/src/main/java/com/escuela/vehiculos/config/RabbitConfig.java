package com.escuela.vehiculos.config;

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
 * Configuracion de RabbitMQ para MS-Vehiculos.
 *
 * Topologia:
 *   vehiculos.exchange (topic, durable)
 *     └─ binding "vehiculos.#" ──► vehiculos.queue ──(on failure)──► vehiculos.dlx ──► vehiculos.dlq
 */
@Configuration
public class RabbitConfig extends AbstractRabbitConfig {

    public static final String EXCHANGE_NAME = "vehiculos.exchange";
    public static final String QUEUE_NAME = "vehiculos.queue";
    public static final String DLX_NAME = "vehiculos.dlx";
    public static final String DLQ_NAME = "vehiculos.dlq";
    public static final String ROUTING_KEY = "vehiculos.#";

    @Bean
    public TopicExchange vehiculosExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue vehiculosQueue() {
        return QueueBuilder.durable(QUEUE_NAME)
                .withArgument("x-dead-letter-exchange", DLX_NAME)
                .build();
    }

    @Bean
    public DirectExchange vehiculosDlx() {
        return new DirectExchange(DLX_NAME, true, false);
    }

    @Bean
    public Queue vehiculosDlq() {
        return QueueBuilder.durable(DLQ_NAME).build();
    }

    @Bean
    public Binding vehiculosQueueBinding() {
        return BindingBuilder.bind(vehiculosQueue()).to(vehiculosExchange()).with(ROUTING_KEY);
    }

    @Bean
    public Binding vehiculosDlqBinding() {
        return BindingBuilder.bind(vehiculosDlq()).to(vehiculosDlx()).with("");
    }
}
