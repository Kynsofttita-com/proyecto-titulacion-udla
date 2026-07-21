package com.escuela.cobros.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request para crear/editar un movimiento contable MANUAL
 * (ingreso u gasto registrado desde la UI, no auto-generado desde un pago).
 */
public record MovimientoContableRequest(
        @NotNull(message = "La fecha es requerida")
        LocalDate fecha,

        @NotNull(message = "El tipo es requerido")
        @Pattern(regexp = "^(INGRESO|GASTO)$",
                message = "Tipo debe ser INGRESO o GASTO")
        String tipo,

        @NotNull(message = "El monto es requerido")
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
        BigDecimal monto,

        @NotNull(message = "La cuenta es requerida")
        Long cuentaId,

        @NotNull(message = "La categoria es requerida")
        Long categoriaId,

        @Size(max = 255, message = "La descripcion no puede exceder 255 caracteres")
        String descripcion,

        @Size(max = 80, message = "La referencia no puede exceder 80 caracteres")
        String referencia
) {}
