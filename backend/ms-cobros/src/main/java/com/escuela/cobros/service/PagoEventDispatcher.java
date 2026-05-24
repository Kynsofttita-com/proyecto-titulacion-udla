package com.escuela.cobros.service;

import com.escuela.cobros.config.RabbitConfig;
import com.escuela.cobros.entity.Factura;
import com.escuela.cobros.entity.Pago;
import com.escuela.common.events.cobros.PagoRegistradoEvent;
import com.escuela.common.events.publisher.EventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PagoEventDispatcher {

    private final ObjectProvider<RabbitTemplate> rabbitTemplateProvider;
    private final String applicationName;
    private volatile EventPublisher cached;

    public PagoEventDispatcher(
        ObjectProvider<RabbitTemplate> rabbitTemplateProvider,
        @Value("${spring.application.name:ms-cobros}") String applicationName
    ) {
        this.rabbitTemplateProvider = rabbitTemplateProvider;
        this.applicationName = applicationName;
    }

    public void publishRegistrado(Pago pago, Factura factura) {
        try {
            EventPublisher publisher = resolvePublisher();
            if (publisher == null) {
                log.warn("RabbitTemplate no disponible, evento no publicado");
                return;
            }

            PagoRegistradoEvent event = new PagoRegistradoEvent(
                pago.getId(),
                factura.getId(),
                factura.getEstudianteId(),
                pago.getMonto(),
                pago.getMetodoPago(),
                pago.getFechaPago(),
                factura.getEstado()
            );

            publisher.publish(RabbitConfig.EXCHANGE_NAME, "pago.registrado", event);
            log.info("Evento PagoRegistradoEvent publicado para pago: {}", pago.getId());
        } catch (Exception e) {
            log.error("Error publicando evento PagoRegistradoEvent", e);
        }
    }

    private EventPublisher resolvePublisher() {
        if (cached != null) {
            return cached;
        }

        RabbitTemplate template = rabbitTemplateProvider.getIfAvailable();
        if (template == null) {
            return null;
        }

        synchronized (this) {
            if (cached == null) {
                cached = new EventPublisher(template, applicationName);
            }
        }
        return cached;
    }
}
