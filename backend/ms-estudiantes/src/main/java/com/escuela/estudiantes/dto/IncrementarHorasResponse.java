package com.escuela.estudiantes.dto;

/**
 * Respuesta tras incrementar minutos completados.
 * - {@code transicionAutomatica}: true si el estudiante alcanzó las horas del
 *   curso y se cambió a COMPLETADO automáticamente.
 */
public record IncrementarHorasResponse(
        Long estudianteId,
        Integer minutosAnteriores,
        Integer minutosActuales,
        String estadoActual,
        Boolean transicionAutomatica,
        String observacion
) {}
