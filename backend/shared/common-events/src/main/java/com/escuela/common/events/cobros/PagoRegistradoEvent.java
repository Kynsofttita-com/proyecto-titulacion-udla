package com.escuela.common.events.cobros;

import com.escuela.common.events.BaseEvent;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Evento publicado cuando se registra un pago contra una factura.
 *
 * <p>Routing key: {@code pago.registrado}<br>
 * Exchange: {@code cobros.exchange} (topic, durable).</p>
 *
 * <p>Consumido por MS-Estudiantes para:
 * <ul>
 *   <li>Auto-transicion {@code PRE_MATRICULADO} → {@code MATRICULADO} al primer pago.</li>
 *   <li>Actualizar {@code situacion_pago} segun el estado resultante de la factura.</li>
 * </ul>
 *
 * <p>{@code estadoFactura} contiene el estado de la factura DESPUES de aplicar
 * este pago: {@code PARCIAL} (saldo > 0) o {@code PAGADA} (saldo = 0).</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PagoRegistradoEvent extends BaseEvent {

    public static final String ROUTING_KEY = "pago.registrado";

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
}
