package com.escuela.asignaciones.service;

import com.escuela.asignaciones.config.RabbitConfig;
import com.escuela.asignaciones.entity.Asignacion;
import com.escuela.common.events.asignaciones.AsignacionCreadaEvent;
import com.escuela.common.events.asignaciones.AsignacionReprogramadaEvent;
import com.escuela.common.events.asignaciones.AsignacionCanceladaEvent;
import com.escuela.common.events.publisher.EventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

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

    public void publishReprogramada(Asignacion asignacion, LocalDateTime fechaHoraAnterior) {
        try {
            EventPublisher publisher = resolvePublisher();
            if (publisher == null) {
                log.warn("RabbitTemplate no disponible, evento no publicado");
                return;
            }

            AsignacionReprogramadaEvent event = new AsignacionReprogramadaEvent(
                    asignacion.getId(),
                    asignacion.getInstructorId(),
                    asignacion.getEstudianteId(),
                    asignacion.getVehiculoId(),
                    fechaHoraAnterior,
                    asignacion.getFechaHora(),
                    asignacion.getDuracionMinutos()
            );

            publisher.publish(RabbitConfig.EXCHANGE_NAME, "asignacion.reprogramada", event);
            log.info("Evento AsignacionReprogramada publicado: id={}", asignacion.getId());
        } catch (Exception e) {
            log.error("Error publicando evento AsignacionReprogramada", e);
        }
    }

    public void publishCancelada(Asignacion asignacion) {
        try {
            EventPublisher publisher = resolvePublisher();
            if (publisher == null) {
                log.warn("RabbitTemplate no disponible, evento no publicado");
                return;
            }

            AsignacionCanceladaEvent event = new AsignacionCanceladaEvent(
                    asignacion.getId(),
                    asignacion.getInstructorId(),
                    asignacion.getEstudianteId(),
                    asignacion.getVehiculoId(),
                    asignacion.getFechaHora(),
                    asignacion.getMotivoCancelacion()
            );

            publisher.publish(RabbitConfig.EXCHANGE_NAME, "asignacion.cancelada", event);
            log.info("Evento AsignacionCancelada publicado: id={}", asignacion.getId());
        } catch (Exception e) {
            log.error("Error publicando evento AsignacionCancelada", e);
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
