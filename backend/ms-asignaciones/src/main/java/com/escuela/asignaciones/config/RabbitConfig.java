package com.escuela.asignaciones.config;

import com.escuela.common.events.config.AbstractRabbitConfig;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracion de RabbitMQ para MS-Asignaciones.
 * Hereda Jackson converter + RabbitTemplate de AbstractRabbitConfig.
 * Declara su TopicExchange de dominio: asignaciones.exchange
 */
@Configuration
public class RabbitConfig extends AbstractRabbitConfig {

    public static final String EXCHANGE_NAME = "asignaciones.exchange";

    @Bean
    public TopicExchange asignacionesExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }
}
