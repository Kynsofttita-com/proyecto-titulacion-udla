package com.escuela.vehiculos.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TipoCombustibleResponse(
        Long id,
        String codigo,
        String nombre,
        String unidad,
        BigDecimal precioActual,
        Boolean activo,
        String observaciones,
        LocalDateTime updatedAt,
        String updatedBy
) {}
