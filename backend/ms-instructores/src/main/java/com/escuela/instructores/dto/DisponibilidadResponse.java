package com.escuela.instructores.dto;

import java.time.LocalTime;

public record DisponibilidadResponse(
        Long id,
        Short diaSemana,
        LocalTime horaInicio,
        LocalTime horaFin
) {}
