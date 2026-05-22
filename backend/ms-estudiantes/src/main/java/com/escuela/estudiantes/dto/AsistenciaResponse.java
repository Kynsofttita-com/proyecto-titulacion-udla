package com.escuela.estudiantes.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AsistenciaResponse(
        Long id,
        Long estudianteId,
        Long asignacionId,
        LocalDate fechaClase,
        Boolean asistio,
        String justificacion,
        String observaciones,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
