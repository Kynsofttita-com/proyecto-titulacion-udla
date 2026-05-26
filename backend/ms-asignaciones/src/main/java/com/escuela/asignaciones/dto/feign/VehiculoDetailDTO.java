package com.escuela.asignaciones.dto.feign;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Subset de los campos de Vehiculo que ms-asignaciones necesita para validar
 * que un vehículo puede ser asignado a una clase.
 */
public record VehiculoDetailDTO(
        Long id,
        String placa,
        String estado,                  // ACTIVO | MANTENIMIENTO | FUERA_SERVICIO
        Integer kilometraje,
        Long categoriaLicenciaId,       // categoría requerida para conducirlo
        LocalDate soatVencimiento,
        LocalDate revisionVencimiento,
        LocalDateTime deletedAt
) {}
