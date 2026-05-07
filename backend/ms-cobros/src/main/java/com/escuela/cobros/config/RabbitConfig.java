package com.escuela.cobros.config;

import com.escuela.common.events.config.AbstractRabbitConfig;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracion de RabbitMQ para MS-Cobros.
 * Hereda Jackson converter + RabbitTemplate de AbstractRabbitConfig.
 * Declara su TopicExchange de dominio: cobros.exchange
 */
@Configuration
public class RabbitConfig extends AbstractRabbitConfig {

    public static final String EXCHANGE_NAME = "cobros.exchange";

    @Bean
    public TopicExchange cobrosExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }
}
