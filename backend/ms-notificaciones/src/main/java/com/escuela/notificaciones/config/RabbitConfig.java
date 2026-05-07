package com.escuela.notificaciones.config;

import com.escuela.common.events.config.AbstractRabbitConfig;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracion de RabbitMQ para MS-Notificaciones.
 * Hereda Jackson converter + RabbitTemplate de AbstractRabbitConfig.
 * Declara su TopicExchange de dominio: notificaciones.exchange
 */
@Configuration
public class RabbitConfig extends AbstractRabbitConfig {

    public static final String EXCHANGE_NAME = "notificaciones.exchange";

    @Bean
    public TopicExchange notificacionesExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }
}
