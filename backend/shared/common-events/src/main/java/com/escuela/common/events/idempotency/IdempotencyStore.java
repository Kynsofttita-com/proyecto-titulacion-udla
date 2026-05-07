package com.escuela.common.events.idempotency;

import java.util.UUID;

/**
 * Contrato para verificar y marcar la idempotencia de eventos consumidos.
 *
 * <p>Cada microservicio que actua como consumer debe proveer una implementacion
 * (tipicamente respaldada por la tabla {@code shared_schema.processed_events}).
 * Asi se garantiza que un evento con el mismo {@code eventId} no se procese
 * dos veces, incluso si RabbitMQ lo reentrega tras una caida del consumer.</p>
 */
public interface IdempotencyStore {

    /**
     * @return {@code true} si el evento ya fue procesado por este consumer,
     *         {@code false} si nunca se ha visto.
     */
    boolean isProcessed(UUID eventId);

    /**
     * Marca el evento como procesado. Llamar SOLO tras procesar exitosamente.
     *
     * @param eventId           UUID unico del evento (de {@code BaseEvent.eventId})
     * @param microservicio     nombre del consumer (ej: "ms-notificaciones")
     * @param tipoEvento        tipo logico del evento (ej: "auth.usuario.creado")
     */
    void markProcessed(UUID eventId, String microservicio, String tipoEvento);
}
