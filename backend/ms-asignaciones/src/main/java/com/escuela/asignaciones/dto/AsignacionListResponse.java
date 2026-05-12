package com.escuela.asignaciones.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

public record AsignacionListResponse(
        Long id,
        Long estudianteId,
        Long instructorId,
        Long vehiculoId,
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin,
        String estado,
        LocalDateTime dateCreated
) {}
