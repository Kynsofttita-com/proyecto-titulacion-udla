package com.escuela.auth.config;

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
 * Configuracion de RabbitMQ para MS-Auth.
 *
 * Topologia:
 *   auth.exchange (topic, durable)
 *     └─ binding "auth.#" ──► auth.queue ──(fail)──► auth.dlx ──► auth.dlq
 *
 *   estudiantes.exchange + instructores.exchange (declarados tambien por sus MS)
 *     └─ bindings ──► auth.from-domains.queue
 *        (para crear automaticamente Usuario al recibir esos eventos)
 */
@Configuration
public class RabbitConfig extends AbstractRabbitConfig {

    public static final String EXCHANGE_NAME = "auth.exchange";
    public static final String QUEUE_NAME = "auth.queue";
    public static final String DLX_NAME = "auth.dlx";
    public static final String DLQ_NAME = "auth.dlq";
    public static final String ROUTING_KEY = "auth.#";

    public static final String ESTUDIANTES_EXCHANGE = "estudiantes.exchange";
    public static final String INSTRUCTORES_EXCHANGE = "instructores.exchange";
    public static final String FROM_DOMAINS_QUEUE_NAME = "auth.from-domains.queue";
    public static final String ESTUDIANTES_CREADO_ROUTING = "estudiantes.creado";
    public static final String INSTRUCTORES_CREADO_ROUTING = "instructores.creado";

    @Bean
    public TopicExchange authExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue authQueue() {
        return QueueBuilder.durable(QUEUE_NAME)
                .withArgument("x-dead-letter-exchange", DLX_NAME)
                .build();
    }

    @Bean
    public DirectExchange authDlx() {
        return new DirectExchange(DLX_NAME, true, false);
    }

    @Bean
    public Queue authDlq() {
        return QueueBuilder.durable(DLQ_NAME).build();
    }

    @Bean
    public Binding authQueueBinding() {
        return BindingBuilder.bind(authQueue()).to(authExchange()).with(ROUTING_KEY);
    }

    @Bean
    public Binding authDlqBinding() {
        return BindingBuilder.bind(authDlq()).to(authDlx()).with("");
    }

    // ============ Consume eventos de dominio (Estudiantes / Instructores) ============

    @Bean
    public TopicExchange estudiantesExchangeRef() {
        return new TopicExchange(ESTUDIANTES_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange instructoresExchangeRef() {
        return new TopicExchange(INSTRUCTORES_EXCHANGE, true, false);
    }

    @Bean
    public Queue authFromDomainsQueue() {
        return QueueBuilder.durable(FROM_DOMAINS_QUEUE_NAME)
                .withArgument("x-dead-letter-exchange", DLX_NAME)
                .build();
    }

    @Bean
    public Binding authFromEstudiantesBinding() {
        return BindingBuilder.bind(authFromDomainsQueue())
                .to(estudiantesExchangeRef())
                .with(ESTUDIANTES_CREADO_ROUTING);
    }

    @Bean
    public Binding authFromInstructoresBinding() {
        return BindingBuilder.bind(authFromDomainsQueue())
                .to(instructoresExchangeRef())
                .with(INSTRUCTORES_CREADO_ROUTING);
    }
}
