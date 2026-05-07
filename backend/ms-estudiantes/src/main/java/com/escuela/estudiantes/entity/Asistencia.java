package com.escuela.estudiantes.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Registro de asistencia de un estudiante a una clase específica.
 *
 * <p>Una fila por estudiante por asignación (constraint UNIQUE en BD).
 * {@code asignacionId} referencia {@code asignaciones_schema.asignaciones}
 * sin FK cross-schema.</p>
 */
@Entity
@Table(name = "asistencia", schema = "estudiantes_schema")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Asistencia implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    @Column(name = "asignacion_id", nullable = false)
    private Long asignacionId;

    @Column(name = "fecha_clase", nullable = false)
    private LocalDate fechaClase;

    @Column(nullable = false)
    private Boolean asistio;

    @Column(columnDefinition = "TEXT")
    private String justificacion;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by", length = 50, updatable = false)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by", length = 50)
    private String updatedBy;
}
