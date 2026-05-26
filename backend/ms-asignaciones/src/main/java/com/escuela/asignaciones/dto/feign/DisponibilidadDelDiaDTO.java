package com.escuela.asignaciones.dto.feign;

import java.time.LocalTime;
import java.util.List;

/**
 * Respuesta del endpoint /instructores/{id}/disponibilidad?fecha= en ms-instructores.
 * Combina horario semanal recurrente + excepciones EXTRA/AUSENCIA.
 */
public record DisponibilidadDelDiaDTO(
        Long instructorId,
        String fecha,
        Short diaSemana,
        Boolean disponible,            // false si AUSENCIA total
        String motivoNoDisponible,
        List<FranjaHoraria> franjas
) {
    public record FranjaHoraria(
            LocalTime horaInicio,
            LocalTime horaFin
    ) {}
}
