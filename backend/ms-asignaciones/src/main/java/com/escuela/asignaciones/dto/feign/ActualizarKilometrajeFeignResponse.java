package com.escuela.asignaciones.dto.feign;

/**
 * Respuesta que recibimos de ms-vehiculos al actualizar kilometraje.
 */
public record ActualizarKilometrajeFeignResponse(
        Long vehiculoId,
        Integer kmAnterior,
        Integer kmActual,
        Boolean aplicado,
        String observacion
) {}
