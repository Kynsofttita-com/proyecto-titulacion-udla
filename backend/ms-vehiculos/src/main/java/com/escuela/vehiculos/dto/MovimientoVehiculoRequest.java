package com.escuela.vehiculos.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Payload que ms-vehiculos envia a ms-cobros para sincronizar el gasto
 * contable asociado a un registro de combustible o mantenimiento.
 * Refleja el DTO homonimo del lado de ms-cobros.
 */
public record MovimientoVehiculoRequest(
        LocalDate fecha,
        BigDecimal monto,
        Long vehiculoId,
        String placaVehiculo,
        Integer kilometraje,
        String descripcion,
        String referencia
) {}
