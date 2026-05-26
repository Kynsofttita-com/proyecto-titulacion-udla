package com.escuela.estudiantes.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Body para PUT /estudiantes/{id}/horas-completadas/incrementar.
 * Lo invoca ms-asignaciones cuando un instructor finaliza una clase, sumando
 * los minutos de duración real de esa clase al acumulado del estudiante.
 */
public record IncrementarHorasRequest(
        @NotNull(message = "minutos requerido")
        @Min(value = 1, message = "minutos debe ser >= 1")
        Integer minutos,

        /** Trazabilidad: ej "ASIGNACION_42" para auditoría. */
        @Size(max = 100)
        String fuente
) {}
