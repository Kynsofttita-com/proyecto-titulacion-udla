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
 * Topologia propia:
 *   notificaciones.exchange (topic, durable)
 *     └─ binding "notificaciones.#" ──► notificaciones.queue ──(on failure)──► notificaciones.dlx ──► notificaciones.dlq
 *
 * Bindings cross-domain (suscripciones a eventos de otros MS):
 *   auth.exchange ─["auth.usuario.creado"]─► notificaciones.queue
 *     (consume eventos publicados por MS-Auth para enviar email de bienvenida
 *      y crear preferencias de notificacion por defecto)
 */
@Configuration
public class RabbitConfig extends AbstractRabbitConfig {

    public static final String EXCHANGE_NAME = "notificaciones.exchange";
    public static final String QUEUE_NAME = "notificaciones.queue";
    public static final String DLX_NAME = "notificaciones.dlx";
    public static final String DLQ_NAME = "notificaciones.dlq";
    public static final String ROUTING_KEY = "notificaciones.#";

    /** Nombre del exchange de MS-Auth (declarado tambien alla, idempotente). */
    public static final String AUTH_EXCHANGE_NAME = "auth.exchange";

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

    // -----------------------------------------------------------------------
    // Bindings cross-domain (suscripciones a eventos de otros MS)
    // -----------------------------------------------------------------------

    /**
     * Referencia al exchange de MS-Auth. Lo declaramos aqui tambien para
     * poder bindear nuestra queue. RabbitMQ trata las declaraciones como
     * idempotentes: si el exchange ya existe con los mismos parametros,
     * no se modifica.
     */
    @Bean
    public TopicExchange authExchangeReference() {
        return new TopicExchange(AUTH_EXCHANGE_NAME, true, false);
    }

    /**
     * Suscribe {@code notificaciones.queue} al evento
     * {@code auth.usuario.creado} publicado por MS-Auth.
     */
    @Bean
    public Binding authUsuarioCreadoBinding() {
        return BindingBuilder.bind(notificacionesQueue())
                .to(authExchangeReference())
                .with("auth.usuario.creado");
    }
}
