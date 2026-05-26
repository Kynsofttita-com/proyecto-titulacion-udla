package com.escuela.vehiculos.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CrearTipoCombustibleRequest(
        @NotBlank(message = "codigo requerido")
        @Pattern(regexp = "^[A-Z][A-Z0-9_]{1,19}$",
                message = "codigo debe ser MAYUSCULAS, empezar con letra, 2-20 chars (ej: EXTRA, GLP_AUTO)")
        String codigo,

        @NotBlank(message = "nombre requerido")
        @Size(min = 2, max = 100)
        String nombre,

        @NotBlank(message = "unidad requerida")
        @Pattern(regexp = "^(GALON|LITRO|KWH)$", message = "unidad debe ser GALON, LITRO o KWH")
        String unidad,

        @NotNull(message = "precioActual requerido")
        @DecimalMin(value = "0.0001", message = "precioActual debe ser > 0")
        BigDecimal precioActual,

        String observaciones
) {}
