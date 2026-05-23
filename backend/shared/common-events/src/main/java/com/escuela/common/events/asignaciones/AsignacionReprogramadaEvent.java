package com.escuela.common.events.asignaciones;

import com.escuela.common.events.BaseEvent;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class AsignacionReprogramadaEvent extends BaseEvent {

    @JsonProperty("asignacion_id")
    private Long asignacionId;

    @JsonProperty("instructor_id")
    private Long instructorId;

    @JsonProperty("estudiante_id")
    private Long estudianteId;

    @JsonProperty("vehiculo_id")
    private Long vehiculoId;

    @JsonProperty("fecha_hora_anterior")
    private LocalDateTime fechaHoraAnterior;

    @JsonProperty("fecha_hora_nueva")
    private LocalDateTime fechaHoraNueva;

    @JsonProperty("duracion_minutos")
    private Short duracionMinutos;

    public AsignacionReprogramadaEvent() {
        super();
    }

    public AsignacionReprogramadaEvent(Long asignacionId, Long instructorId, Long estudianteId,
                                       Long vehiculoId, LocalDateTime fechaHoraAnterior,
                                       LocalDateTime fechaHoraNueva, Short duracionMinutos) {
        super();
        this.asignacionId = asignacionId;
        this.instructorId = instructorId;
        this.estudianteId = estudianteId;
        this.vehiculoId = vehiculoId;
        this.fechaHoraAnterior = fechaHoraAnterior;
        this.fechaHoraNueva = fechaHoraNueva;
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

    public LocalDateTime getFechaHoraAnterior() {
        return fechaHoraAnterior;
    }

    public void setFechaHoraAnterior(LocalDateTime fechaHoraAnterior) {
        this.fechaHoraAnterior = fechaHoraAnterior;
    }

    public LocalDateTime getFechaHoraNueva() {
        return fechaHoraNueva;
    }

    public void setFechaHoraNueva(LocalDateTime fechaHoraNueva) {
        this.fechaHoraNueva = fechaHoraNueva;
    }

    public Short getDuracionMinutos() {
        return duracionMinutos;
    }

    public void setDuracionMinutos(Short duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
    }
}
