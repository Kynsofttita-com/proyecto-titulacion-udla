package com.escuela.common.events.asignaciones;

import com.escuela.common.events.BaseEvent;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * Evento publicado por ms-asignaciones cuando una asignacion pasa a estado
 * COMPLETADA (instructor llamo a PATCH /asignaciones/{id}/finalizar).
 *
 * <p>Consumido por ms-estudiantes para incrementar el contador
 * {@code clases_completadas} en la tabla progreso_academico y recalcular
 * la calificacion / aprobacion si aplica.</p>
 */
public class AsignacionCompletadaEvent extends BaseEvent {

    public static final String ROUTING_KEY = "asignacion.completada";

    @JsonProperty("asignacion_id")
    private Long asignacionId;

    @JsonProperty("instructor_id")
    private Long instructorId;

    @JsonProperty("estudiante_id")
    private Long estudianteId;

    @JsonProperty("vehiculo_id")
    private Long vehiculoId;

    @JsonProperty("fecha_hora")
    private LocalDateTime fechaHora;

    @JsonProperty("duracion_real_minutos")
    private Integer duracionRealMinutos;

    @JsonProperty("km_recorridos")
    private Integer kmRecorridos;

    public AsignacionCompletadaEvent() { super(); }

    public AsignacionCompletadaEvent(Long asignacionId, Long instructorId, Long estudianteId,
                                     Long vehiculoId, LocalDateTime fechaHora,
                                     Integer duracionRealMinutos, Integer kmRecorridos) {
        super();
        this.asignacionId = asignacionId;
        this.instructorId = instructorId;
        this.estudianteId = estudianteId;
        this.vehiculoId = vehiculoId;
        this.fechaHora = fechaHora;
        this.duracionRealMinutos = duracionRealMinutos;
        this.kmRecorridos = kmRecorridos;
    }

    public Long getAsignacionId() { return asignacionId; }
    public void setAsignacionId(Long asignacionId) { this.asignacionId = asignacionId; }

    public Long getInstructorId() { return instructorId; }
    public void setInstructorId(Long instructorId) { this.instructorId = instructorId; }

    public Long getEstudianteId() { return estudianteId; }
    public void setEstudianteId(Long estudianteId) { this.estudianteId = estudianteId; }

    public Long getVehiculoId() { return vehiculoId; }
    public void setVehiculoId(Long vehiculoId) { this.vehiculoId = vehiculoId; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public Integer getDuracionRealMinutos() { return duracionRealMinutos; }
    public void setDuracionRealMinutos(Integer duracionRealMinutos) { this.duracionRealMinutos = duracionRealMinutos; }

    public Integer getKmRecorridos() { return kmRecorridos; }
    public void setKmRecorridos(Integer kmRecorridos) { this.kmRecorridos = kmRecorridos; }
}
