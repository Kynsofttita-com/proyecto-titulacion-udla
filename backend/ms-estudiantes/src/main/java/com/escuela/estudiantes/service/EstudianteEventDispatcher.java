package com.escuela.estudiantes.service;

import com.escuela.common.events.BaseEvent;
import com.escuela.common.events.estudiantes.CursoCompletadoEvent;
import com.escuela.common.events.estudiantes.EstudianteActualizadoEvent;
import com.escuela.common.events.estudiantes.EstudianteCreadoEvent;
import com.escuela.common.events.estudiantes.EstudianteEliminadoEvent;
import com.escuela.common.events.estudiantes.EstudianteMatriculadoEvent;
import com.escuela.common.events.publisher.EventPublisher;
import com.escuela.estudiantes.config.RabbitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Despacha eventos de dominio de MS-Estudiantes a RabbitMQ.
 *
 * <p>Patron espejado de {@code AuthEventDispatcher}: construye el
 * {@link EventPublisher} de forma lazy a partir del {@link RabbitTemplate}
 * (resuelto via {@link ObjectProvider} para tolerar contextos de test sin
 * RabbitMQ).</p>
 *
 * <p>Si no hay RabbitTemplate disponible (perfil test), los publish se
 * convierten en no-ops con log warning -- el dominio sigue funcionando.</p>
 */
@Component
public class EstudianteEventDispatcher {

    private static final Logger log = LoggerFactory.getLogger(EstudianteEventDispatcher.class);

    private final ObjectProvider<RabbitTemplate> rabbitTemplateProvider;
    private final String applicationName;
    private volatile EventPublisher cached;

    public EstudianteEventDispatcher(ObjectProvider<RabbitTemplate> rabbitTemplateProvider,
                                     @Value("${spring.application.name:ms-estudiantes}") String applicationName) {
        this.rabbitTemplateProvider = rabbitTemplateProvider;
        this.applicationName = applicationName;
    }

    public void publishCreado(EstudianteCreadoEvent event) {
        publish(EstudianteCreadoEvent.ROUTING_KEY, event);
    }

    public void publishActualizado(EstudianteActualizadoEvent event) {
        publish(EstudianteActualizadoEvent.ROUTING_KEY, event);
    }

    public void publishEliminado(EstudianteEliminadoEvent event) {
        publish(EstudianteEliminadoEvent.ROUTING_KEY, event);
    }

    public void publishMatriculado(EstudianteMatriculadoEvent event) {
        publish(EstudianteMatriculadoEvent.ROUTING_KEY, event);
    }

    public void publishCursoCompletado(CursoCompletadoEvent event) {
        publish(CursoCompletadoEvent.ROUTING_KEY, event);
    }

    private void publish(String routingKey, BaseEvent event) {
        EventPublisher publisher = resolvePublisher();
        if (publisher == null) {
            log.warn("RabbitTemplate no disponible (perfil test o RabbitMQ caido); " +
                    "skip evento routingKey={} payload={}", routingKey, event);
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
