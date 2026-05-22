package com.escuela.estudiantes.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public record CreateAsistenciaRequest(
        @NotNull Long asignacionId,
        @NotNull @PastOrPresent LocalDate fechaClase,
        @NotNull Boolean asistio,
        String justificacion,
        String observaciones
) {}
