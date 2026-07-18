package com.escuela.notificaciones.config;

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
 * Configuracion de RabbitMQ para MS-Notificaciones.
 *
 * Topologia propia:
 *   notificaciones.exchange (topic, durable)
 *     └─ binding "notificaciones.#" ──► notificaciones.queue ──(on failure)──► notificaciones.dlx ──► notificaciones.dlq
 *
 * Bindings cross-domain (suscripciones a eventos de otros MS):
 *   auth.exchange ─["auth.usuario.creado"]─► notificaciones.queue
 *     (consume eventos publicados por MS-Auth para enviar email de bienvenida
 *      y crear preferencias de notificacion por defecto)
 */
@Configuration
public class RabbitConfig extends AbstractRabbitConfig {

    public static final String EXCHANGE_NAME = "notificaciones.exchange";
    public static final String QUEUE_NAME = "notificaciones.queue";
    public static final String OPERATIVO_QUEUE_NAME = "notificaciones.operativo.queue";
    public static final String DLX_NAME = "notificaciones.dlx";
    public static final String DLQ_NAME = "notificaciones.dlq";
    public static final String ROUTING_KEY = "notificaciones.#";

    /** Nombre del exchange de MS-Auth (declarado tambien alla, idempotente). */
    public static final String AUTH_EXCHANGE_NAME = "auth.exchange";

    /** Nombre del exchange de MS-Vehiculos. */
    public static final String VEHICULOS_EXCHANGE_NAME = "vehiculos.exchange";

    /** Nombre del exchange de MS-Instructores. */
    public static final String INSTRUCTORES_EXCHANGE_NAME = "instructores.exchange";

    /** Nombre del exchange de MS-Estudiantes. */
    public static final String ESTUDIANTES_EXCHANGE_NAME = "estudiantes.exchange";

    /** Nombre del exchange de MS-Cobros. */
    public static final String COBROS_EXCHANGE_NAME = "cobros.exchange";

    @Bean
    public TopicExchange notificacionesExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue notificacionesQueue() {
        return QueueBuilder.durable(QUEUE_NAME)
                .withArgument("x-dead-letter-exchange", DLX_NAME)
                .build();
    }

    @Bean
    public DirectExchange notificacionesDlx() {
        return new DirectExchange(DLX_NAME, true, false);
    }

    @Bean
    public Queue notificacionesDlq() {
        return QueueBuilder.durable(DLQ_NAME).build();
    }

    @Bean
    public Binding notificacionesQueueBinding() {
        return BindingBuilder.bind(notificacionesQueue()).to(notificacionesExchange()).with(ROUTING_KEY);
    }

    @Bean
    public Binding notificacionesDlqBinding() {
        return BindingBuilder.bind(notificacionesDlq()).to(notificacionesDlx()).with("");
    }

    // -----------------------------------------------------------------------
    // Bindings cross-domain (suscripciones a eventos de otros MS)
    // -----------------------------------------------------------------------

    /**
     * Referencia al exchange de MS-Auth. Lo declaramos aqui tambien para
     * poder bindear nuestra queue. RabbitMQ trata las declaraciones como
     * idempotentes: si el exchange ya existe con los mismos parametros,
     * no se modifica.
     */
    @Bean
    public TopicExchange authExchangeReference() {
        return new TopicExchange(AUTH_EXCHANGE_NAME, true, false);
    }

    /**
     * Suscribe {@code notificaciones.queue} al evento
     * {@code auth.usuario.creado} publicado por MS-Auth.
     */
    @Bean
    public Binding authUsuarioCreadoBinding() {
        return BindingBuilder.bind(notificacionesQueue())
                .to(authExchangeReference())
                .with("auth.usuario.creado");
    }

    /**
     * Suscribe a {@code auth.password.reset.solicitado} para enviar email
     * con link de recuperacion (Sprint 4 / T4.4).
     */
    @Bean
    public Binding authPasswordResetSolicitadoBinding() {
        return BindingBuilder.bind(notificacionesQueue())
                .to(authExchangeReference())
                .with("auth.password.reset.solicitado");
    }

    /**
     * Suscribe a {@code auth.usuario.bloqueado} para notificar al usuario
     * que su cuenta fue bloqueada por intentos fallidos (Sprint 4 / T4.4).
     */
    @Bean
    public Binding authUsuarioBloqueadoBinding() {
        return BindingBuilder.bind(notificacionesQueue())
                .to(authExchangeReference())
                .with("auth.usuario.bloqueado");
    }

    // -----------------------------------------------------------------------
    // Bindings a exchanges operativos (vencimientos, atrasos, curso completado)
    // -----------------------------------------------------------------------

    @Bean
    public TopicExchange vehiculosExchangeReference() {
        return new TopicExchange(VEHICULOS_EXCHANGE_NAME, true, false);
    }

    @Bean
    public TopicExchange instructoresExchangeReference() {
        return new TopicExchange(INSTRUCTORES_EXCHANGE_NAME, true, false);
    }

    @Bean
    public TopicExchange estudiantesExchangeReference() {
        return new TopicExchange(ESTUDIANTES_EXCHANGE_NAME, true, false);
    }

    @Bean
    public TopicExchange cobrosExchangeReference() {
        return new TopicExchange(COBROS_EXCHANGE_NAME, true, false);
    }

    /**
     * Queue dedicada para eventos operativos (SOAT, licencia, curso, pagos).
     * Separada de {@link #QUEUE_NAME} para que las dos clases de listener no
     * compitan por los mismos mensajes (round-robin de Spring AMQP causaria
     * que un evento SOAT llegue al listener auth y caiga en el
     * {@code @RabbitHandler(isDefault=true)}).
     */
    @Bean
    public Queue notificacionesOperativoQueue() {
        return QueueBuilder.durable(OPERATIVO_QUEUE_NAME)
                .withArgument("x-dead-letter-exchange", DLX_NAME)
                .build();
    }

    @Bean
    public Binding vehiculosSoatVencimientoBinding() {
        return BindingBuilder.bind(notificacionesOperativoQueue())
                .to(vehiculosExchangeReference())
                .with("vehiculos.soat.vencimiento.proximo");
    }

    @Bean
    public Binding instructoresLicenciaVencimientoBinding() {
        return BindingBuilder.bind(notificacionesOperativoQueue())
                .to(instructoresExchangeReference())
                .with("instructores.licencia.vencimiento.proximo");
    }

    @Bean
    public Binding estudiantesCursoCompletadoBinding() {
        return BindingBuilder.bind(notificacionesOperativoQueue())
                .to(estudiantesExchangeReference())
                .with("estudiantes.curso.completado");
    }

    @Bean
    public Binding cobrosPagoAtrasadoBinding() {
        return BindingBuilder.bind(notificacionesOperativoQueue())
                .to(cobrosExchangeReference())
                .with("cobros.pago.atrasado");
    }
}
