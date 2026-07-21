package com.escuela.cobros.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record PagoRequest(
    @NotNull(message = "El ID de la factura no puede ser nulo")
    Long facturaId,

    @NotNull(message = "El monto no puede ser nulo")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    BigDecimal monto,

    @NotNull(message = "El método de pago no puede ser nulo")
    @Pattern(
        regexp = "^(EFECTIVO|TRANSFERENCIA|TARJETA)$",
        message = "El método de pago debe ser EFECTIVO, TRANSFERENCIA o TARJETA"
    )
    String metodoPago,

    /**
     * Cuenta contable a la que ingreso el pago.
     * Requerido en pagos nuevos; los historicos previos a la migracion V3
     * quedaron con cuenta_id=NULL.
     */
    @NotNull(message = "La cuenta contable es requerida")
    Long cuentaId,

    String referenciaTransaccion,
    String observaciones
) {}
