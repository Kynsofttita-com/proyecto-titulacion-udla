package com.escuela.instructores.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record DisponibilidadRequest(
        @NotNull @Min(1) @Max(7) Short diaSemana,
        @NotNull LocalTime horaInicio,
        @NotNull LocalTime horaFin
) {}
