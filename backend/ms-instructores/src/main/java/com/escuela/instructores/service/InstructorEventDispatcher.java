package com.escuela.instructores.service;

import com.escuela.common.events.BaseEvent;
import com.escuela.common.events.instructores.InstructorCreadoEvent;
import com.escuela.common.events.publisher.EventPublisher;
import com.escuela.instructores.config.RabbitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Despacha eventos de dominio de MS-Instructores a RabbitMQ.
 * Espejo de EstudianteEventDispatcher.
 */
@Component
public class InstructorEventDispatcher {

    private static final Logger log = LoggerFactory.getLogger(InstructorEventDispatcher.class);

    private final ObjectProvider<RabbitTemplate> rabbitTemplateProvider;
    private final String applicationName;
    private volatile EventPublisher cached;

    public InstructorEventDispatcher(ObjectProvider<RabbitTemplate> rabbitTemplateProvider,
                                     @Value("${spring.application.name:ms-instructores}") String applicationName) {
        this.rabbitTemplateProvider = rabbitTemplateProvider;
        this.applicationName = applicationName;
    }

    public void publishCreado(InstructorCreadoEvent event) {
        publish(InstructorCreadoEvent.ROUTING_KEY, event);
    }

    private void publish(String routingKey, BaseEvent event) {
        EventPublisher publisher = resolvePublisher();
        if (publisher == null) {
            log.warn("RabbitTemplate no disponible; skip evento routingKey={} payload={}",
                    routingKey, event);
            return;
        }
        publisher.publish(RabbitConfig.EXCHANGE_NAME, routingKey, event);
    }

    private EventPublisher resolvePublisher() {
        if (cached != null) return cached;
        RabbitTemplate template = rabbitTemplateProvider.getIfAvailable();
        if (template == null) return null;
        synchronized (this) {
            if (cached == null) {
                cached = new EventPublisher(template, applicationName);
            }
        }
        return cached;
    }
}
