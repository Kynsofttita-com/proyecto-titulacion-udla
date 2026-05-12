package com.escuela.asignaciones.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record UpdateAsignacionRequest(
        Long estudianteId,
        Long instructorId,
        Long vehiculoId,
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin,
        String estado,
        String observaciones
) {}
