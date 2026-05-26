package com.escuela.asignaciones.dto.feign;

/**
 * Subset que ms-asignaciones consume de GET /estudiantes/{id}.
 * Jackson ignora silenciosamente los campos extra que devuelve el ms-estudiantes.
 */
public record EstudianteDetailDTO(
        Long id,
        String estado,
        Long categoriaLicenciaId,
        Integer horasCompletadas
) {}
