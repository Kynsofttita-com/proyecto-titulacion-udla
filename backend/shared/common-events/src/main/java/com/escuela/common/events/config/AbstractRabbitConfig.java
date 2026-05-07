package com.escuela.common.events.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;

/**
 * Configuracion base de RabbitMQ compartida por todos los microservicios.
 *
 * Cada microservicio extiende esta clase con su propia clase {@code RabbitConfig}
 * anotada con {@code @Configuration} y declara su {@code TopicExchange} de dominio.
 * Asi se evita duplicar la configuracion del converter JSON y del RabbitTemplate.
 *
 * Provee:
 *   - Jackson2JsonMessageConverter con soporte para java.time (Instant, LocalDate)
 *     y fechas serializadas como ISO-8601 string (no timestamps).
 *   - RabbitTemplate con el converter aplicado.
 *
 * Lo que NO provee (cada MS lo declara):
 *   - TopicExchange propio del dominio.
 *   - Queues, bindings y DLQs (se agregan en T3.2 / T3.3).
 */
public abstract class AbstractRabbitConfig {

    /**
     * Converter JSON usado por publishers y consumers.
     * Usa un ObjectMapper local (no el global de Spring MVC) para evitar acoplar
     * la serializacion REST con la serializacion AMQP.
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(mapper);
    }

    /**
     * RabbitTemplate compartido para publicar mensajes con serializacion JSON.
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
