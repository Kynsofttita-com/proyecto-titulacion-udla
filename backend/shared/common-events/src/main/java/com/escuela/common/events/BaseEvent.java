package com.escuela.common.events;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Clase base para todos los eventos publicados a RabbitMQ.
 * Provee metadatos comunes: id único, timestamp, microservicio origen y versión del evento.
 *
 * <p>Los eventos concretos heredan de esta clase usando {@link SuperBuilder} de Lombok
 * para preservar la inmutabilidad y permitir construcción fluida.</p>
 *
 * @see com.escuela.common.events.auth.UsuarioCreadoEvent
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class BaseEvent implements Serializable {

    /** Identificador único del evento (para idempotencia en consumidores). */
    private UUID eventId;

    /** Momento exacto en que se generó el evento (UTC). */
    private Instant timestamp;

    /** Microservicio que publicó el evento (ej: "ms-estudiantes"). */
    private String source;

    /** Versión del esquema del evento (para evolución backward-compatible). */
    private String version;
}
