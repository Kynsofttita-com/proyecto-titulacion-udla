package com.escuela.estudiantes.dto;

import java.math.BigDecimal;

/**
 * Respuesta del endpoint GET /estudiantes/{id}/progreso.
 *
 * <p>Combina valores almacenados en la tabla {@code progreso_academico}
 * (contadores mantenidos por listener) con valores derivados calculados
 * on-the-fly:</p>
 *
 * <ul>
 *   <li>{@code horasCompletadas}: derivado de {@code estudiantes.minutos_completados / 60}</li>
 *   <li>{@code horasRequeridas}: del tipoCurso del estudiante (Feign a ms-auth)</li>
 *   <li>{@code porcentajeComplecion}: {@code (horasCompletadas / horasRequeridas) * 100}, max 100</li>
 *   <li>{@code asignacionesCompletadas}: igual a {@code clasesCompletadas}</li>
 *   <li>{@code asignacionesPendientes}: {@code planeadas - completadas - canceladas}</li>
 * </ul>
 */
public record ProgresoAcademicoResponse(
        Long id,
        Long estudianteId,

        // === Contadores almacenados en la tabla progreso_academico ===
        Short clasesPlaneadas,
        Short clasesCompletadas,
        Short clasesPendientes,
        Short clasesCanceladas,
        BigDecimal calificacionPromedio,
        Boolean aprobado,

        // === Campos derivados (calculados on-the-fly) ===
        BigDecimal horasCompletadas,
        Integer horasRequeridas,
        Integer porcentajeComplecion,
        Integer asignacionesCompletadas,
        Integer asignacionesPendientes
) {}
