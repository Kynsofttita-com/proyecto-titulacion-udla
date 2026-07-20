package com.escuela.asignaciones.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Total de horas de clases COMPLETADA por un instructor en un rango de fechas.
 * Consumido por ms-instructores (ResumenHorasService) via Feign.
 */
public record HorasCumplidasResponse(
        Long instructorId,
        LocalDate desde,
        LocalDate hasta,
        long clasesCompletadas,
        long minutosCumplidos,
        BigDecimal horasCumplidas
) {}
