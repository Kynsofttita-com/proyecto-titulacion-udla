package com.escuela.cobros.event;

import com.escuela.common.events.BaseEvent;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CobroRegistradoEvent extends BaseEvent {

    @JsonProperty("pago_id")
    private Long pagoId;

    @JsonProperty("factura_id")
    private Long facturaId;

    @JsonProperty("estudiante_id")
    private Long estudianteId;

    @JsonProperty("monto")
    private BigDecimal monto;

    @JsonProperty("metodo_pago")
    private String metodoPago;

    @JsonProperty("fecha_pago")
    private LocalDateTime fechaPago;

    @JsonProperty("estado_factura")
    private String estadoFactura;

    public CobroRegistradoEvent() {
        super();
    }

    public CobroRegistradoEvent(Long pagoId, Long facturaId, Long estudianteId,
                               BigDecimal monto, String metodoPago,
                               LocalDateTime fechaPago, String estadoFactura) {
        super();
        this.pagoId = pagoId;
        this.facturaId = facturaId;
        this.estudianteId = estudianteId;
        this.monto = monto;
        this.metodoPago = metodoPago;
        this.fechaPago = fechaPago;
        this.estadoFactura = estadoFactura;
    }

    public Long getPagoId() {
        return pagoId;
    }

    public void setPagoId(Long pagoId) {
        this.pagoId = pagoId;
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

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDateTime fechaPago) {
        this.fechaPago = fechaPago;
    }

    public String getEstadoFactura() {
        return estadoFactura;
    }

    public void setEstadoFactura(String estadoFactura) {
        this.estadoFactura = estadoFactura;
    }
}
