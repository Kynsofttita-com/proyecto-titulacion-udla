package com.escuela.vehiculos.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ActualizarPrecioRequest(
        @NotNull(message = "precioActual requerido")
        @DecimalMin(value = "0.0001", message = "precioActual debe ser > 0")
        BigDecimal precioActual,

        Boolean activo,

        String observaciones
) {}
