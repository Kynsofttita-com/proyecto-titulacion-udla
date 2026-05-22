package com.escuela.instructores.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record HorarioTrabajoResponse(
        Long id,
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin,
        String tipo,
        String motivo
) {}
