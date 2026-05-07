package com.escuela.asignaciones.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Registro inmutable de cambios a una asignación (reprogramaciones).
 * Append-only: nunca se actualiza ni se borra.
 */
@Entity
@Table(name = "cambios_asignacion", schema = "asignaciones_schema")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CambioAsignacion implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asignacion_id", nullable = false)
    private Asignacion asignacion;

    @Column(name = "fecha_cambio", nullable = false)
    private LocalDateTime fechaCambio = LocalDateTime.now();

    @Column(nullable = false, columnDefinition = "TEXT")
    private String motivo;

    @Column(name = "fecha_anterior")
    private LocalDateTime fechaAnterior;

    @Column(name = "fecha_nueva")
    private LocalDateTime fechaNueva;

    @Column(name = "instructor_anterior")
    private Long instructorAnterior;

    @Column(name = "instructor_nuevo")
    private Long instructorNuevo;

    @Column(name = "vehiculo_anterior")
    private Long vehiculoAnterior;

    @Column(name = "vehiculo_nuevo")
    private Long vehiculoNuevo;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(name = "created_by", length = 50, updatable = false)
    private String createdBy;
}
