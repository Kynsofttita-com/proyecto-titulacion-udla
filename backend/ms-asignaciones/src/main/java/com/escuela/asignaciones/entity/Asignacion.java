package com.escuela.asignaciones.entity;

import com.escuela.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Clase tripartita: instructor + estudiante + vehículo en una fecha/hora dada.
 *
 * <p>Las referencias a {@code instructorId}, {@code estudianteId} y
 * {@code vehiculoId} apuntan a otros microservicios sin FK cross-schema.</p>
 *
 * <p>Usa optimistic locking via {@code @Version} para evitar conflictos al
 * reprogramar/cancelar concurrentemente.</p>
 */
@Entity
@Table(name = "asignaciones", schema = "asignaciones_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Asignacion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "instructor_id", nullable = false)
    private Long instructorId;

    @Column(name = "estudiante_id", nullable = false)
    private Long estudianteId;

    @Column(name = "vehiculo_id", nullable = false)
    private Long vehiculoId;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "duracion_minutos", nullable = false)
    private Short duracionMinutos = 60;

    @Column(nullable = false, length = 20)
    private String estado = "PROGRAMADA";

    @Column(name = "tipo_clase", nullable = false, length = 20)
    private String tipoClase;

    private String ubicacion;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "motivo_cancelacion", columnDefinition = "TEXT")
    private String motivoCancelacion;

    @Version
    @Column(nullable = false)
    private Long version = 0L;
}
