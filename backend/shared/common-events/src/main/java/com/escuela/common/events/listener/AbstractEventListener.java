package com.escuela.common.events.listener;

import com.escuela.common.events.BaseEvent;
import com.escuela.common.events.idempotency.IdempotencyStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clase base para consumers de eventos con idempotencia automatica.
 *
 * <p>Las subclases concretas declaran {@code @RabbitListener} sobre la queue
 * que les interesa y, dentro del handler, delegan a
 * {@link #processWithIdempotency(BaseEvent, String)} para que se ejecute la
 * logica solo una vez por {@code eventId}.</p>
 *
 * <p>Si {@link #handle(BaseEvent)} lanza una excepcion, no se marca como
 * procesado y Spring AMQP aplicara el retry configurado; tras agotar intentos,
 * el mensaje termina en la DLQ del dominio.</p>
 *
 * @param <E> tipo concreto del evento (subclase de {@link BaseEvent})
 */
public abstract class AbstractEventListener<E extends BaseEvent> {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private final IdempotencyStore idempotencyStore;
    private final String microservicio;

    protected AbstractEventListener(IdempotencyStore idempotencyStore, String microservicio) {
        this.idempotencyStore = idempotencyStore;
        this.microservicio = microservicio;
    }

    /**
     * Procesa el evento aplicando idempotencia: si {@code event.eventId} ya
     * existe en {@link IdempotencyStore} se descarta; si no, se invoca
     * {@link #handle(BaseEvent)} y al finalizar se registra como procesado.
     *
     * @param event       evento recibido (debe tener {@code eventId} no nulo)
     * @param tipoEvento  identificador logico del evento (ej: routing key)
     */
    protected final void processWithIdempotency(E event, String tipoEvento) {
        if (event.getEventId() == null) {
            log.warn("Evento sin eventId recibido (tipo={}); se procesa sin idempotencia", tipoEvento);
            handle(event);
            return;
        }

        if (idempotencyStore.isProcessed(event.getEventId())) {
            log.info("Evento {} ya procesado (tipo={}, source={}); skip",
                    event.getEventId(), tipoEvento, event.getSource());
            return;
        }

        handle(event);
        idempotencyStore.markProcessed(event.getEventId(), microservicio, tipoEvento);
        log.debug("Evento {} marcado como procesado (tipo={})", event.getEventId(), tipoEvento);
    }

    /**
     * Logica de negocio especifica del consumer. Si lanza excepcion, el
     * evento NO se marca como procesado y se aplicara retry/DLQ.
     */
    protected abstract void handle(E event);
}
