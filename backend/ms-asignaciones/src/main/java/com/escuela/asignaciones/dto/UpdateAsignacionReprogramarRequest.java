package com.escuela.asignaciones.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record UpdateAsignacionReprogramarRequest(
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin,
        String motivoCancelacion
) {}
