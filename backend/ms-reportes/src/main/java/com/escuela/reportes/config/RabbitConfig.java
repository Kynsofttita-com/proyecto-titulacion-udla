package com.escuela.reportes.config;

import com.escuela.common.events.config.AbstractRabbitConfig;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracion de RabbitMQ para MS-Reportes.
 * Hereda Jackson converter + RabbitTemplate de AbstractRabbitConfig.
 * Declara su TopicExchange de dominio: reportes.exchange
 */
@Configuration
public class RabbitConfig extends AbstractRabbitConfig {

    public static final String EXCHANGE_NAME = "reportes.exchange";

    @Bean
    public TopicExchange reportesExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }
}
