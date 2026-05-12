package com.escuela.asignaciones.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;

public record CreateAsignacionRequest(
        @NotNull(message = "ID estudiante requerido")
        Long estudianteId,

        @NotNull(message = "ID instructor requerido")
        Long instructorId,

        @NotNull(message = "ID vehículo requerido")
        Long vehiculoId,

        @NotNull(message = "Fecha requerida")
        @FutureOrPresent(message = "Fecha debe ser presente o futura")
        LocalDate fecha,

        @NotNull(message = "Hora inicio requerida")
        LocalTime horaInicio,

        @NotNull(message = "Hora fin requerida")
        LocalTime horaFin,

        String observaciones
) {}
