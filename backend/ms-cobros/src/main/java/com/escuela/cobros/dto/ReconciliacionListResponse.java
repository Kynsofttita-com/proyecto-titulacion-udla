package com.escuela.cobros.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReconciliacionListResponse(
    Long id,
    LocalDate fecha,
    BigDecimal totalDia,
    Integer cantidadPagos,
    LocalDateTime createdAt
) {}
