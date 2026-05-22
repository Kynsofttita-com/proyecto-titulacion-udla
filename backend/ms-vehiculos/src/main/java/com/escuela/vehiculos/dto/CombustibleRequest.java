package com.escuela.vehiculos.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CombustibleRequest(
        @NotNull @PastOrPresent LocalDateTime fecha,
        @NotNull @DecimalMin(value = "0.01", message = "Litros debe ser > 0") BigDecimal litros,
        @NotNull @DecimalMin(value = "0.01", message = "Costo debe ser > 0") BigDecimal costoTotal,
        @NotNull @Min(0) Integer kilometrajeActual,
        String estacion
) {}
