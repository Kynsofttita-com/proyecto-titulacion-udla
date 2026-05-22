package com.escuela.vehiculos.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CombustibleResponse(
        Long id,
        Long vehiculoId,
        LocalDateTime fecha,
        BigDecimal litros,
        BigDecimal costoTotal,
        BigDecimal costoPorLitro,
        Integer kilometrajeActual,
        String estacion
) {}
