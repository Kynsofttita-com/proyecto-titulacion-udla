package com.escuela.asignaciones.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Body para PATCH /asignaciones/{id}/iniciar.
 * Si {@code kmInicial} es null, el backend lo tomara del kilometraje actual
 * del vehiculo (asumiendo que el instructor olvido marcar el inicio).
 */
public record IniciarAsignacionRequest(
        @Min(value = 0, message = "kmInicial no puede ser negativo")
        Integer kmInicial,

        String observaciones
) {}
