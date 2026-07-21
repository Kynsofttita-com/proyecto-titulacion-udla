package com.escuela.cobros.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CategoriaMovimientoRequest(
        @NotBlank(message = "El codigo es requerido")
        @Pattern(regexp = "^[A-Z][A-Z0-9_]{1,39}$",
                message = "El codigo debe ser MAYUSCULAS, empezar con letra, 2-40 chars")
        String codigo,

        @NotBlank(message = "El nombre es requerido")
        @Size(min = 2, max = 80, message = "El nombre debe tener entre 2 y 80 caracteres")
        String nombre,

        @NotBlank(message = "El tipo es requerido")
        @Pattern(regexp = "^(INGRESO|GASTO)$",
                message = "Tipo debe ser INGRESO o GASTO")
        String tipo,

        Boolean activo
) {}
