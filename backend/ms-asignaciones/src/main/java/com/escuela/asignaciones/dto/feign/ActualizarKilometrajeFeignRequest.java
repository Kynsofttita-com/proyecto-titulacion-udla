package com.escuela.asignaciones.dto.feign;

/**
 * Body que enviamos a ms-vehiculos en PATCH /vehiculos/{id}/kilometraje.
 * Refleja {@code ActualizarKilometrajeRequest} de ese MS.
 */
public record ActualizarKilometrajeFeignRequest(
        Integer nuevoKm,
        String fuente
) {}
