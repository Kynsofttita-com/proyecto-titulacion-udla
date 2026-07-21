package com.escuela.cobros.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CuentaResponse(
        Long id,
        String nombre,
        String tipo,
        String numeroCuenta,
        BigDecimal saldoInicial,
        BigDecimal saldoActual,
        Boolean activo,
        String observaciones,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy
) {}
