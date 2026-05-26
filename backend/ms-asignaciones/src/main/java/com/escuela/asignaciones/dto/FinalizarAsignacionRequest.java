package com.escuela.asignaciones.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Body para PATCH /asignaciones/{id}/finalizar.
 * El kmFinal es obligatorio. Si la clase no se habia iniciado, se asume
 * estado EN_CURSO ad-hoc y se completa en una sola operacion.
 */
public record FinalizarAsignacionRequest(
        @NotNull(message = "kmFinal es requerido al finalizar la clase")
        @Min(value = 0, message = "kmFinal no puede ser negativo")
        Integer kmFinal,

        String observacionesRecorrido
) {}
