package com.escuela.instructores.config;

import com.escuela.common.events.config.AbstractRabbitConfig;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracion de RabbitMQ para MS-Instructores.
 * Hereda Jackson converter + RabbitTemplate de AbstractRabbitConfig.
 * Declara su TopicExchange de dominio: instructores.exchange
 */
@Configuration
public class RabbitConfig extends AbstractRabbitConfig {

    public static final String EXCHANGE_NAME = "instructores.exchange";

    @Bean
    public TopicExchange instructoresExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }
}
