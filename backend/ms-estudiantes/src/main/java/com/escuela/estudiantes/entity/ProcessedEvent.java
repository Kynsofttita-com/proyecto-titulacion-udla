package com.escuela.estudiantes.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "processed_events", schema = "shared_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessedEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true)
    private UUID eventId;

    @Column(name = "microservicio_consumidor", nullable = false, length = 50)
    private String microservicioConsumidor;

    @Column(name = "tipo_evento", nullable = false, length = 100)
    private String tipoEvento;

    @CreationTimestamp
    @Column(name = "processed_at", nullable = false, updatable = false)
    private LocalDateTime processedAt;
}
