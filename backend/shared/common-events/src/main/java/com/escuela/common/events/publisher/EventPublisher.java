package com.escuela.common.events.publisher;

import com.escuela.common.events.BaseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.Instant;
import java.util.UUID;

/**
 * Publisher generico de eventos a RabbitMQ.
 *
 * <p>Auto-completa los metadatos de {@link BaseEvent} (eventId, timestamp,
 * source, version) si vienen nulos, antes de delegar en
 * {@link RabbitTemplate#convertAndSend}.</p>
 *
 * <p>Se registra como bean automaticamente via
 * {@code EventPublisherAutoConfiguration} cuando hay un {@link RabbitTemplate}
 * disponible en el contexto. En tests con perfil "test" no se crea (porque
 * {@code RabbitAutoConfiguration} esta excluida y no hay RabbitTemplate).</p>
 */
public class EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);
    private static final String DEFAULT_VERSION = "1.0";

    private final RabbitTemplate rabbitTemplate;
    private final String applicationName;

    public EventPublisher(RabbitTemplate rabbitTemplate, String applicationName) {
        this.rabbitTemplate = rabbitTemplate;
        this.applicationName = applicationName;
    }

    /**
     * Publica un evento al exchange y routing key indicados, autocompletando
     * los metadatos faltantes.
     */
    public void publish(String exchange, String routingKey, BaseEvent event) {
        autoFillMetadata(event);
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
        log.info("Evento publicado: exchange={}, routingKey={}, eventId={}, source={}",
                exchange, routingKey, event.getEventId(), event.getSource());
    }

    private void autoFillMetadata(BaseEvent event) {
        if (event.getEventId() == null) {
            event.setEventId(UUID.randomUUID());
        }
        if (event.getTimestamp() == null) {
            event.setTimestamp(Instant.now());
        }
        if (event.getSource() == null) {
            event.setSource(applicationName);
        }
        if (event.getVersion() == null) {
            event.setVersion(DEFAULT_VERSION);
        }
    }
}
