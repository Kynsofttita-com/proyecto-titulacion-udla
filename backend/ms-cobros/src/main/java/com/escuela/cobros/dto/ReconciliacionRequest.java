package com.escuela.cobros.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReconciliacionRequest(
    @NotNull(message = "La fecha de reconciliación no puede ser nula")
    LocalDate fecha,

    @NotNull(message = "El total en efectivo no puede ser nulo")
    @DecimalMin(value = "0", message = "El total en efectivo no puede ser negativo")
    BigDecimal totalEfectivo,

    @NotNull(message = "El total en tarjeta no puede ser nulo")
    @DecimalMin(value = "0", message = "El total en tarjeta no puede ser negativo")
    BigDecimal totalTarjeta,

    @NotNull(message = "El total en transferencia no puede ser nulo")
    @DecimalMin(value = "0", message = "El total en transferencia no puede ser negativo")
    BigDecimal totalTransferencia,

    @NotNull(message = "El total en cheque no puede ser nulo")
    @DecimalMin(value = "0", message = "El total en cheque no puede ser negativo")
    BigDecimal totalCheque,

    @Min(value = 0, message = "La cantidad de pagos no puede ser negativa")
    Integer cantidadPagos,

    String observaciones
) {}
