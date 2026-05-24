package com.escuela.common.events.asignaciones;

import com.escuela.common.events.BaseEvent;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class AsignacionCreadaEvent extends BaseEvent {

    public static final String ROUTING_KEY = "asignacion.creada";

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

    @JsonProperty("duracion_minutos")
    private Short duracionMinutos;

    public AsignacionCreadaEvent() {
        super();
    }

    public AsignacionCreadaEvent(Long asignacionId, Long instructorId, Long estudianteId,
                                 Long vehiculoId, LocalDateTime fechaHora, Short duracionMinutos) {
        super();
        this.asignacionId = asignacionId;
        this.instructorId = instructorId;
        this.estudianteId = estudianteId;
        this.vehiculoId = vehiculoId;
        this.fechaHora = fechaHora;
        this.duracionMinutos = duracionMinutos;
    }

    public Long getAsignacionId() {
        return asignacionId;
    }

    public void setAsignacionId(Long asignacionId) {
        this.asignacionId = asignacionId;
    }

    public Long getInstructorId() {
        return instructorId;
    }

    public void setInstructorId(Long instructorId) {
        this.instructorId = instructorId;
    }

    public Long getEstudianteId() {
        return estudianteId;
    }

    public void setEstudianteId(Long estudianteId) {
        this.estudianteId = estudianteId;
    }

    public Long getVehiculoId() {
        return vehiculoId;
    }

    public void setVehiculoId(Long vehiculoId) {
        this.vehiculoId = vehiculoId;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public Short getDuracionMinutos() {
        return duracionMinutos;
    }

    public void setDuracionMinutos(Short duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
    }
}
