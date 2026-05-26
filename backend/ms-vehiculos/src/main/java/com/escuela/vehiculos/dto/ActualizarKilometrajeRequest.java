package com.escuela.vehiculos.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Solo actualiza el kilometraje del vehiculo. Idempotente y monotonico:
 * si el nuevoKm <= km actual, no hace nada (no se permite retroceder odometro).
 * Usado por ms-asignaciones cuando un instructor finaliza una clase.
 */
public record ActualizarKilometrajeRequest(
        @NotNull(message = "nuevoKm requerido")
        @Min(value = 0, message = "Kilometraje no puede ser negativo")
        Integer nuevoKm,

        /** Trazabilidad. Ej: "ASIGNACION_42" o "MANUAL_ADMIN". */
        @Size(max = 100)
        String fuente
) {}
