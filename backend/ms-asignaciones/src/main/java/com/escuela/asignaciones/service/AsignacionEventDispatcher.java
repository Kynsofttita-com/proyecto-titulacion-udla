package com.escuela.asignaciones.service;

import com.escuela.asignaciones.config.RabbitConfig;
import com.escuela.asignaciones.entity.Asignacion;
import com.escuela.common.events.asignaciones.AsignacionCreadaEvent;
import com.escuela.common.events.publisher.EventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AsignacionEventDispatcher {

    private final ObjectProvider<RabbitTemplate> rabbitTemplateProvider;
    private final String applicationName;
    private volatile EventPublisher cached;

    public AsignacionEventDispatcher(ObjectProvider<RabbitTemplate> rabbitTemplateProvider,
                                    @Value("${spring.application.name:ms-asignaciones}") String applicationName) {
        this.rabbitTemplateProvider = rabbitTemplateProvider;
        this.applicationName = applicationName;
    }

    public void publishCreada(Asignacion asignacion) {
        try {
            EventPublisher publisher = resolvePublisher();
            if (publisher == null) {
                log.warn("RabbitTemplate no disponible, evento no publicado");
                return;
            }

            AsignacionCreadaEvent event = new AsignacionCreadaEvent(
                    asignacion.getId(),
                    asignacion.getInstructorId(),
                    asignacion.getEstudianteId(),
                    asignacion.getVehiculoId(),
                    asignacion.getFechaHora(),
                    asignacion.getDuracionMinutos()
            );

            publisher.publish(RabbitConfig.EXCHANGE_NAME, "asignacion.creada", event);
            log.info("Evento AsignacionCreada publicado: id={}", asignacion.getId());
        } catch (Exception e) {
            log.error("Error publicando evento AsignacionCreada", e);
        }
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
