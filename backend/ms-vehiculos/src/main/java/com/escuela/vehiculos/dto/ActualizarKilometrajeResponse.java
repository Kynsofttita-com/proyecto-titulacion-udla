package com.escuela.vehiculos.dto;

/**
 * Respuesta tras intentar actualizar kilometraje.
 * - {@code aplicado=true}: el km se aumento al nuevoKm.
 * - {@code aplicado=false}: no se actualizo (nuevoKm <= kmAnterior).
 */
public record ActualizarKilometrajeResponse(
        Long vehiculoId,
        Integer kmAnterior,
        Integer kmActual,
        Boolean aplicado,
        String observacion
) {}
