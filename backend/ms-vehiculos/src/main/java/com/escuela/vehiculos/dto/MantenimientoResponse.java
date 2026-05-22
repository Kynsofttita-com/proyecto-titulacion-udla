package com.escuela.vehiculos.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MantenimientoResponse(
        Long id,
        Long vehiculoId,
        String tipo,
        LocalDate fecha,
        BigDecimal costo,
        String descripcion,
        String taller,
        Integer kilometrajeServicio,
        LocalDate proximaFecha
) {}
