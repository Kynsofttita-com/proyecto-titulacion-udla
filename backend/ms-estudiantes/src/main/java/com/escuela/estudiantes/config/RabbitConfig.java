package com.escuela.estudiantes.config;

import com.escuela.common.events.config.AbstractRabbitConfig;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracion de RabbitMQ para MS-Estudiantes.
 *
 * Topologia:
 *   estudiantes.exchange (topic, durable)
 *     └─ binding "estudiantes.#" ──► estudiantes.queue ──(fail)──► estudiantes.dlx ──► estudiantes.dlq
 *
 *   auth.exchange (topic, durable, declarado tambien por MS-Auth)
 *     └─ binding "auth.usuario.creado" ──► estudiantes.from-auth.queue
 *        (para enlazar usuario_id al recibir UsuarioCreadoEvent)
 *
 *   cobros.exchange (topic, durable, declarado por MS-Cobros)
 *     └─ binding "pago.registrado" ──► estudiantes.from-cobros.queue
 *        (auto-transicion PRE_MATRICULADO → MATRICULADO + situacion_pago)
 *
 *   asignaciones.exchange (topic, durable, declarado por MS-Asignaciones)
 *     └─ binding "asignacion.creada" ──► estudiantes.from-asignaciones.queue
 *        (auto-transicion MATRICULADO → CURSANDO)
 */
@Configuration
public class RabbitConfig extends AbstractRabbitConfig {

    public static final String EXCHANGE_NAME = "estudiantes.exchange";
    public static final String QUEUE_NAME = "estudiantes.queue";
    public static final String DLX_NAME = "estudiantes.dlx";
    public static final String DLQ_NAME = "estudiantes.dlq";
    public static final String ROUTING_KEY = "estudiantes.#";

    public static final String AUTH_EXCHANGE_NAME = "auth.exchange";
    public static final String FROM_AUTH_QUEUE_NAME = "estudiantes.from-auth.queue";
    public static final String AUTH_USUARIO_CREADO_ROUTING = "auth.usuario.creado";

    public static final String COBROS_EXCHANGE_NAME = "cobros.exchange";
    public static final String FROM_COBROS_QUEUE_NAME = "estudiantes.from-cobros.queue";
    public static final String COBROS_PAGO_REGISTRADO_ROUTING = "pago.registrado";

    public static final String FROM_COBROS_FACTURAS_QUEUE_NAME = "estudiantes.from-cobros-facturas.queue";
    public static final String COBROS_FACTURA_EMITIDA_ROUTING = "factura.emitida";

    public static final String ASIGNACIONES_EXCHANGE_NAME = "asignaciones.exchange";
    public static final String FROM_ASIGNACIONES_QUEUE_NAME = "estudiantes.from-asignaciones.queue";
    public static final String ASIGNACIONES_CREADA_ROUTING = "asignacion.creada";

    // Cola dedicada al mantenimiento del progreso academico. Recibe todos los
    // eventos del ciclo de vida de una asignacion (creada/completada/cancelada)
    // para mantener los contadores en la tabla progreso_academico actualizados.
    public static final String FROM_ASIGNACIONES_PROGRESO_QUEUE_NAME = "estudiantes.from-asignaciones-progreso.queue";
    public static final String ASIGNACIONES_COMPLETADA_ROUTING = "asignacion.completada";
    public static final String ASIGNACIONES_CANCELADA_ROUTING = "asignacion.cancelada";

    @Bean
    public TopicExchange estudiantesExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue estudiantesQueue() {
        return QueueBuilder.durable(QUEUE_NAME)
                .withArgument("x-dead-letter-exchange", DLX_NAME)
                .build();
    }

    @Bean
    public DirectExchange estudiantesDlx() {
        return new DirectExchange(DLX_NAME, true, false);
    }

    @Bean
    public Queue estudiantesDlq() {
        return QueueBuilder.durable(DLQ_NAME).build();
    }

    @Bean
    public Binding estudiantesQueueBinding() {
        return BindingBuilder.bind(estudiantesQueue()).to(estudiantesExchange()).with(ROUTING_KEY);
    }

    @Bean
    public Binding estudiantesDlqBinding() {
        return BindingBuilder.bind(estudiantesDlq()).to(estudiantesDlx()).with("");
    }

    // ============ Consume de auth.exchange ============

    @Bean
    public TopicExchange authExchangeRef() {
        return new TopicExchange(AUTH_EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue estudiantesFromAuthQueue() {
        return QueueBuilder.durable(FROM_AUTH_QUEUE_NAME)
                .withArgument("x-dead-letter-exchange", DLX_NAME)
                .build();
    }

    @Bean
    public Binding estudiantesFromAuthBinding() {
        return BindingBuilder.bind(estudiantesFromAuthQueue())
                .to(authExchangeRef())
                .with(AUTH_USUARIO_CREADO_ROUTING);
    }

    // ============ Consume de cobros.exchange (pago.registrado) ============

    @Bean
    public TopicExchange cobrosExchangeRef() {
        return new TopicExchange(COBROS_EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue estudiantesFromCobrosQueue() {
        return QueueBuilder.durable(FROM_COBROS_QUEUE_NAME)
                .withArgument("x-dead-letter-exchange", DLX_NAME)
                .build();
    }

    @Bean
    public Binding estudiantesFromCobrosBinding() {
        return BindingBuilder.bind(estudiantesFromCobrosQueue())
                .to(cobrosExchangeRef())
                .with(COBROS_PAGO_REGISTRADO_ROUTING);
    }

    /**
     * Cola dedicada para factura.emitida (separada de pago.registrado para
     * que un fallo procesando una factura no bloquee el procesamiento de pagos).
     */
    @Bean
    public Queue estudiantesFromCobrosFacturasQueue() {
        return QueueBuilder.durable(FROM_COBROS_FACTURAS_QUEUE_NAME)
                .withArgument("x-dead-letter-exchange", DLX_NAME)
                .build();
    }

    @Bean
    public Binding estudiantesFromCobrosFacturasBinding() {
        return BindingBuilder.bind(estudiantesFromCobrosFacturasQueue())
                .to(cobrosExchangeRef())
                .with(COBROS_FACTURA_EMITIDA_ROUTING);
    }

    // ============ Consume de asignaciones.exchange (asignacion.creada) ============

    @Bean
    public TopicExchange asignacionesExchangeRef() {
        return new TopicExchange(ASIGNACIONES_EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue estudiantesFromAsignacionesQueue() {
        return QueueBuilder.durable(FROM_ASIGNACIONES_QUEUE_NAME)
                .withArgument("x-dead-letter-exchange", DLX_NAME)
                .build();
    }

    @Bean
    public Binding estudiantesFromAsignacionesBinding() {
        return BindingBuilder.bind(estudiantesFromAsignacionesQueue())
                .to(asignacionesExchangeRef())
                .with(ASIGNACIONES_CREADA_ROUTING);
    }

    // ============ Cola para mantenimiento del progreso academico ============
    // Escucha creada/completada/cancelada en una sola queue. El listener usa
    // @RabbitHandler por tipo para distinguir.

    @Bean
    public Queue estudiantesFromAsignacionesProgresoQueue() {
        return QueueBuilder.durable(FROM_ASIGNACIONES_PROGRESO_QUEUE_NAME)
                .withArgument("x-dead-letter-exchange", DLX_NAME)
                .build();
    }

    @Bean
    public Binding estudiantesProgresoCreadaBinding() {
        return BindingBuilder.bind(estudiantesFromAsignacionesProgresoQueue())
                .to(asignacionesExchangeRef())
                .with(ASIGNACIONES_CREADA_ROUTING);
    }

    @Bean
    public Binding estudiantesProgresoCompletadaBinding() {
        return BindingBuilder.bind(estudiantesFromAsignacionesProgresoQueue())
                .to(asignacionesExchangeRef())
                .with(ASIGNACIONES_COMPLETADA_ROUTING);
    }

    @Bean
    public Binding estudiantesProgresoCanceladaBinding() {
        return BindingBuilder.bind(estudiantesFromAsignacionesProgresoQueue())
                .to(asignacionesExchangeRef())
                .with(ASIGNACIONES_CANCELADA_ROUTING);
    }
}
