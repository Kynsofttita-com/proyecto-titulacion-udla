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
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Progreso académico de un estudiante. Una fila por estudiante.
 *
 * <p>Se actualiza vía consumo de eventos {@code asignaciones.completada} para
 * mantener contadores agregados sin queries pesadas.</p>
 */
@Entity
@Table(name = "progreso_academico", schema = "estudiantes_schema")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ProgresoAcademico implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estudiante_id", nullable = false, unique = true)
    private Estudiante estudiante;

    @Column(name = "clases_planeadas", nullable = false)
    private Short clasesPlaneadas = 0;

    @Column(name = "clases_completadas", nullable = false)
    private Short clasesCompletadas = 0;

    @Column(name = "clases_pendientes", nullable = false)
    private Short clasesPendientes = 0;

    @Column(name = "clases_canceladas", nullable = false)
    private Short clasesCanceladas = 0;

    @Column(name = "calificacion_promedio", precision = 4, scale = 2)
    private BigDecimal calificacionPromedio;

    private Boolean aprobado;

    // ----- Audit fields (sin deleted_at) -----
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
