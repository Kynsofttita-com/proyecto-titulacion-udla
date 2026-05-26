package com.escuela.instructores.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Resumen de horas trabajadas por un instructor en un rango de fechas.
 * Se usa en el panel de detalle y en futuros reportes de nomina.
 */
public record ResumenHorasResponse(
        Long instructorId,
        LocalDate desde,
        LocalDate hasta,
        String tipoContrato,
        Short horasContratoSemanales,
        /** Horas teoricamente cubiertas por el contrato en el rango. */
        BigDecimal horasContratadas,
        /** Horas efectivamente cumplidas (clases COMPLETADAS en el rango). */
        BigDecimal horasCumplidas,
        BigDecimal horasRestantes,
        BigDecimal porcentajeCumplimiento,
        BigDecimal sueldoEstimado,
        String observacion
) {}
