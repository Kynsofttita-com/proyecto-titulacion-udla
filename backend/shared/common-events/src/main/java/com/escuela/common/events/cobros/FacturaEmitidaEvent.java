package com.escuela.common.events.cobros;

import com.escuela.common.events.BaseEvent;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * Evento publicado cuando MS-Cobros emite una factura nueva.
 *
 * <p>Routing key: {@code factura.emitida}<br>
 * Exchange: {@code cobros.exchange} (topic, durable).</p>
 *
 * <p>Consumido por MS-Estudiantes para:
 * <ul>
 *   <li>Recalcular {@code situacion_pago} del estudiante:
 *       <ul>
 *         <li>{@code CONTADO} → {@code PENDIENTE_PAGO} (factura emitida, $0 pagado)</li>
 *         <li>{@code CREDITO} → {@code PAGADO_TOTAL} (asume debito automatico mensual)</li>
 *       </ul>
 *   </li>
 *   <li>Auto-transicion {@code PRE_MATRICULADO} → {@code MATRICULADO} si la
 *       factura CREDITO emitida ya implica PAGADO_TOTAL (puede empezar clases).</li>
 * </ul>
 *
 * <p>Regla de negocio (Sprint 9 ext): factura CREDITO se considera "pagada"
 * desde el momento de la emision porque se asume cobro automatico por
 * tarjeta. CONTADO debe esperar a recibir el pago manual.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class FacturaEmitidaEvent extends BaseEvent {

    public static final String ROUTING_KEY = "factura.emitida";

    @JsonProperty("factura_id")
    private Long facturaId;

    @JsonProperty("estudiante_id")
    private Long estudianteId;

    @JsonProperty("numero_factura")
    private String numeroFactura;

    /** {@code CONTADO} o {@code CREDITO}. Determina si el estudiante puede iniciar clases inmediatamente. */
    @JsonProperty("tipo_pago")
    private String tipoPago;

    @JsonProperty("monto_original")
    private BigDecimal montoOriginal;

    /** Cantidad de cuotas si es CREDITO; {@code null} o 1 si es CONTADO. */
    @JsonProperty("numero_cuotas")
    private Integer numeroCuotas;
}
