package com.escuela.instructores.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO espejo del expuesto por ms-asignaciones en
 * {@code GET /asignaciones/instructor/{id}/horas-cumplidas}.
 */
public record HorasCumplidasResponse(
        Long instructorId,
        LocalDate desde,
        LocalDate hasta,
        long clasesCompletadas,
        long minutosCumplidos,
        BigDecimal horasCumplidas
) {}
