package com.escuela.cobros.event;

import com.escuela.common.events.BaseEvent;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class CobroCanceladoEvent extends BaseEvent {

    @JsonProperty("factura_id")
    private Long facturaId;

    @JsonProperty("estudiante_id")
    private Long estudianteId;

    @JsonProperty("fecha_cancelacion")
    private LocalDateTime fechaCancelacion;

    @JsonProperty("motivo_cancelacion")
    private String motivoCancelacion;

    public CobroCanceladoEvent() {
        super();
    }

    public CobroCanceladoEvent(Long facturaId, Long estudianteId,
                              LocalDateTime fechaCancelacion, String motivoCancelacion) {
        super();
        this.facturaId = facturaId;
        this.estudianteId = estudianteId;
        this.fechaCancelacion = fechaCancelacion;
        this.motivoCancelacion = motivoCancelacion;
    }

    public Long getFacturaId() {
        return facturaId;
    }

    public void setFacturaId(Long facturaId) {
        this.facturaId = facturaId;
    }

    public Long getEstudianteId() {
        return estudianteId;
    }

    public void setEstudianteId(Long estudianteId) {
        this.estudianteId = estudianteId;
    }

    public LocalDateTime getFechaCancelacion() {
        return fechaCancelacion;
    }

    public void setFechaCancelacion(LocalDateTime fechaCancelacion) {
        this.fechaCancelacion = fechaCancelacion;
    }

    public String getMotivoCancelacion() {
        return motivoCancelacion;
    }

    public void setMotivoCancelacion(String motivoCancelacion) {
        this.motivoCancelacion = motivoCancelacion;
    }
}
