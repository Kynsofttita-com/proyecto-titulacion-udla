package com.escuela.cobros.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AnularMovimientoRequest(
        @NotBlank(message = "El motivo es requerido")
        @Size(min = 5, max = 500, message = "El motivo debe tener entre 5 y 500 caracteres")
        String motivo
) {}
