package com.escuela.vehiculos.service;

import com.escuela.common.events.BaseEvent;
import com.escuela.common.events.publisher.EventPublisher;
import com.escuela.common.events.vehiculos.SoatVencimientoProximoEvent;
import com.escuela.vehiculos.config.RabbitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Despacha eventos de dominio de MS-Vehiculos a RabbitMQ.
 */
@Component
public class VehiculoEventDispatcher {

    private static final Logger log = LoggerFactory.getLogger(VehiculoEventDispatcher.class);

    private final ObjectProvider<RabbitTemplate> rabbitTemplateProvider;
    private final String applicationName;
    private volatile EventPublisher cached;

    public VehiculoEventDispatcher(ObjectProvider<RabbitTemplate> rabbitTemplateProvider,
                                   @Value("${spring.application.name:ms-vehiculos}") String applicationName) {
        this.rabbitTemplateProvider = rabbitTemplateProvider;
        this.applicationName = applicationName;
    }

    public void publishSoatVencimientoProximo(SoatVencimientoProximoEvent event) {
        publish(SoatVencimientoProximoEvent.ROUTING_KEY, event);
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
