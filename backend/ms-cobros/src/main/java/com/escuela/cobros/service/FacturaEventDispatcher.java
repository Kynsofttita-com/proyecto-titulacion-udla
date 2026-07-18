package com.escuela.cobros.service;

import com.escuela.cobros.config.RabbitConfig;
import com.escuela.cobros.entity.Factura;
import com.escuela.cobros.event.CobroCanceladoEvent;
import com.escuela.common.events.cobros.FacturaEmitidaEvent;
import com.escuela.common.events.cobros.PagoAtrasadoEvent;
import com.escuela.common.events.publisher.EventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class FacturaEventDispatcher {

    private final ObjectProvider<RabbitTemplate> rabbitTemplateProvider;
    private final String applicationName;
    private volatile EventPublisher cached;

    public FacturaEventDispatcher(
        ObjectProvider<RabbitTemplate> rabbitTemplateProvider,
        @Value("${spring.application.name:ms-cobros}") String applicationName
    ) {
        this.rabbitTemplateProvider = rabbitTemplateProvider;
        this.applicationName = applicationName;
    }

    /**
     * Publica {@link FacturaEmitidaEvent} tras crear una factura.
     * Consumido por ms-estudiantes para actualizar {@code situacion_pago} y
     * disparar transicion {@code PRE_MATRICULADO -> MATRICULADO} si CREDITO.
     */
    public void publishEmitida(Factura factura) {
        try {
            EventPublisher publisher = resolvePublisher();
            if (publisher == null) {
                log.warn("RabbitTemplate no disponible; FacturaEmitidaEvent no publicado (facturaId={})",
                        factura.getId());
                return;
            }

            FacturaEmitidaEvent event = FacturaEmitidaEvent.builder()
                    .facturaId(factura.getId())
                    .estudianteId(factura.getEstudianteId())
                    .numeroFactura(factura.getNumeroFactura())
                    .tipoPago(factura.getTipoPago())
                    .montoOriginal(factura.getMontoOriginal())
                    .numeroCuotas(factura.getNumeroCuotas())
                    .build();

            publisher.publish(RabbitConfig.EXCHANGE_NAME, FacturaEmitidaEvent.ROUTING_KEY, event);
            log.info("FacturaEmitidaEvent publicado facturaId={} estudianteId={} tipoPago={}",
                    factura.getId(), factura.getEstudianteId(), factura.getTipoPago());
        } catch (Exception e) {
            log.error("Error publicando FacturaEmitidaEvent (facturaId={})", factura.getId(), e);
        }
    }

    /**
     * Publica {@link PagoAtrasadoEvent} disparado por el scheduler diario cuando
     * detecta una factura con cuota vencida.
     */
    public void publishPagoAtrasado(PagoAtrasadoEvent event) {
        try {
            EventPublisher publisher = resolvePublisher();
            if (publisher == null) {
                log.warn("RabbitTemplate no disponible; PagoAtrasadoEvent no publicado (facturaId={})",
                        event.getFacturaId());
                return;
            }
            publisher.publish(RabbitConfig.EXCHANGE_NAME, PagoAtrasadoEvent.ROUTING_KEY, event);
            log.info("PagoAtrasadoEvent publicado facturaId={} estudianteId={} diasAtraso={}",
                    event.getFacturaId(), event.getEstudianteId(), event.getDiasAtraso());
        } catch (Exception e) {
            log.error("Error publicando PagoAtrasadoEvent (facturaId={})", event.getFacturaId(), e);
        }
    }

    public void publishCancelada(Factura factura) {
        try {
            EventPublisher publisher = resolvePublisher();
            if (publisher == null) {
                log.warn("RabbitTemplate no disponible, evento no publicado");
                return;
            }

            CobroCanceladoEvent event = new CobroCanceladoEvent(
                factura.getId(),
                factura.getEstudianteId(),
                LocalDateTime.now(),
                "Factura eliminada"
            );

            publisher.publish(RabbitConfig.EXCHANGE_NAME, "factura.cancelada", event);
            log.info("Evento CobroCanceladoEvent publicado para factura: {}", factura.getId());
        } catch (Exception e) {
            log.error("Error publicando evento CobroCanceladoEvent", e);
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
