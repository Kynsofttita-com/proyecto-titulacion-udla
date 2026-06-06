package com.escuela.estudiantes.dto;

public record ProgresoAcademicoHorasResponse(
    Integer horasCompletadas,
    Integer horasRequeridas,
    Integer porcentajeComplecion,
    Integer asignacionesCompletadas,
    Integer asignacionesPendientes
) {}
