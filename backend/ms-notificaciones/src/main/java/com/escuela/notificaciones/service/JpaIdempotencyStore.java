package com.escuela.notificaciones.service;

import com.escuela.common.events.idempotency.IdempotencyStore;
import com.escuela.notificaciones.entity.ProcessedEvent;
import com.escuela.notificaciones.repository.ProcessedEventRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Implementacion de {@link IdempotencyStore} respaldada por la tabla
 * {@code shared_schema.processed_events} via JPA.
 */
@Service
public class JpaIdempotencyStore implements IdempotencyStore {

    private final ProcessedEventRepository repository;

    public JpaIdempotencyStore(ProcessedEventRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean isProcessed(UUID eventId) {
        return repository.existsByEventId(eventId);
    }

    @Override
    public void markProcessed(UUID eventId, String microservicio, String tipoEvento) {
        ProcessedEvent record = ProcessedEvent.builder()
                .eventId(eventId)
                .microservicioConsumidor(microservicio)
                .tipoEvento(tipoEvento)
                .build();
        repository.save(record);
    }
}
