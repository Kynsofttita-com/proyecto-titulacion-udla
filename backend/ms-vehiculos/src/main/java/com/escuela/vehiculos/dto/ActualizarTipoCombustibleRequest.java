package com.escuela.vehiculos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Actualiza los campos editables de un tipo de combustible ya existente.
 * codigo y unidad son inmutables (identidad del combustible, romper eso
 * invalidaria los datos historicos de carga). Para precio existe el endpoint
 * dedicado PUT /{id}/precio.
 */
public record ActualizarTipoCombustibleRequest(
        @NotBlank(message = "nombre requerido")
        @Size(min = 2, max = 100, message = "nombre entre 2 y 100 caracteres")
        String nombre,

        @Size(max = 500, message = "observaciones maximo 500 caracteres")
        String observaciones
) {}
