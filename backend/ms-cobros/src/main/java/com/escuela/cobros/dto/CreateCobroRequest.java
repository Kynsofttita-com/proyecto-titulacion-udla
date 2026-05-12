package com.escuela.cobros.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateCobroRequest(
        @NotNull(message = "ID estudiante requerido")
        Long estudianteId,

        @NotNull(message = "Monto requerido")
        @DecimalMin(value = "0.01", message = "Monto debe ser mayor a cero")
        BigDecimal monto,

        @NotBlank(message = "Tipo de pago requerido")
        String tipoPago,

        String referencia,
        LocalDate fecha,
        String observaciones
) {}
