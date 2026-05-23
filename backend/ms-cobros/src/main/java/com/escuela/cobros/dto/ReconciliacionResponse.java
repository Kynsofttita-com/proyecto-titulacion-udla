package com.escuela.cobros.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReconciliacionResponse(
    Long id,
    LocalDate fecha,
    BigDecimal totalEfectivo,
    BigDecimal totalTarjeta,
    BigDecimal totalTransferencia,
    BigDecimal totalCheque,
    BigDecimal totalDia,
    Integer cantidadPagos,
    String observaciones,
    Long usuarioConciliadorId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    String createdBy,
    String updatedBy
) {}
