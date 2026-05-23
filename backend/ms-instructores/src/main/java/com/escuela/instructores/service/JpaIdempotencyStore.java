package com.escuela.instructores.service;

import com.escuela.common.events.idempotency.IdempotencyStore;
import com.escuela.instructores.entity.ProcessedEvent;
import com.escuela.instructores.repository.ProcessedEventRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

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
