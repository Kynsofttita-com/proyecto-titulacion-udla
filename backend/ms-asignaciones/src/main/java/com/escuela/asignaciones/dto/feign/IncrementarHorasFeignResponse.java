package com.escuela.asignaciones.dto.feign;

public record IncrementarHorasFeignResponse(
        Long estudianteId,
        Integer minutosAnteriores,
        Integer minutosActuales,
        String estadoActual,
        Boolean transicionAutomatica,
        String observacion
) {}
