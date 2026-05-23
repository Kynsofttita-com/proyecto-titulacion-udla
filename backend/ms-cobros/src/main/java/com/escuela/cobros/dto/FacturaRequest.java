package com.escuela.cobros.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FacturaRequest(
    @NotNull(message = "El ID del estudiante no puede ser nulo")
    Long estudianteId,

    @NotNull(message = "El ID del concepto de facturación no puede ser nulo")
    Long conceptoFacturacionId,

    @NotNull(message = "El monto original no puede ser nulo")
    @DecimalMin(value = "0.01", message = "El monto original debe ser mayor a 0")
    BigDecimal montoOriginal,

    @NotNull(message = "La fecha de vencimiento no puede ser nula")
    @FutureOrPresent(message = "La fecha de vencimiento debe ser presente o futura")
    LocalDate fechaVencimiento,

    String descripcion
) {}
