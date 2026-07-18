package com.escuela.common.events.cobros;

import com.escuela.common.events.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Publicado diariamente por MS-Cobros (scheduler) por cada factura con cuota vencida
 * cuyo saldo es mayor que cero.
 *
 * <p>Routing key: {@code cobros.pago.atrasado}
 * Exchange: {@code cobros.exchange}</p>
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PagoAtrasadoEvent extends BaseEvent {

    public static final String ROUTING_KEY = "cobros.pago.atrasado";

    private Long facturaId;
    private String numeroFactura;
    private Long estudianteId;
    private Long usuarioIdEstudiante;
    private String cedulaEstudiante;
    private String nombreEstudiante;
    private String emailEstudiante;
    private BigDecimal montoTotal;
    private BigDecimal montoPendiente;
    private int diasAtraso;
    private LocalDate fechaVencimientoCuota;
}
