package com.escuela.instructores.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.time.LocalTime;

public record HorarioTrabajoRequest(
        @NotNull LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin,
        @NotBlank
        @Pattern(regexp = "^(EXTRA|AUSENCIA)$", message = "tipo debe ser EXTRA o AUSENCIA")
        String tipo,
        String motivo
) {}
