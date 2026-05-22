package com.escuela.instructores.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Disponibilidad calculada para una fecha especifica.
 * Combina la disponibilidad recurrente del dia de la semana con las
 * excepciones puntuales (EXTRA / AUSENCIA) registradas en horarios_trabajo.
 *
 * <p>Si {@code disponible} es false, {@code franjas} esta vacia.</p>
 */
public record DisponibilidadDelDiaResponse(
        Long instructorId,
        LocalDate fecha,
        Short diaSemana,
        Boolean disponible,
        String motivoNoDisponible,
        List<FranjaHoraria> franjas
) {
    public record FranjaHoraria(LocalTime horaInicio, LocalTime horaFin) {}
}
