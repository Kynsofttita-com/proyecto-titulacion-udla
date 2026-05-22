package com.escuela.vehiculos.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MantenimientoRequest(
        @NotBlank
        @Pattern(regexp = "^(PREVENTIVO|CORRECTIVO)$", message = "tipo PREVENTIVO o CORRECTIVO")
        String tipo,
        @NotNull LocalDate fecha,
        @NotNull @DecimalMin("0.00") BigDecimal costo,
        @NotBlank String descripcion,
        String taller,
        @Min(0) Integer kilometrajeServicio,
        LocalDate proximaFecha
) {}
