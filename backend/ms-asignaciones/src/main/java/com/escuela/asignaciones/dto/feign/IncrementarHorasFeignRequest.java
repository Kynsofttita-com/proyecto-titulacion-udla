package com.escuela.asignaciones.dto.feign;

/**
 * Body que ms-asignaciones envía a ms-estudiantes en
 * PUT /estudiantes/{id}/horas-completadas/incrementar.
 */
public record IncrementarHorasFeignRequest(
        Integer minutos,
        String fuente
) {}
