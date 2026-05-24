package com.escuela.cobros.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record FacturaCuotaResponse(
    Long id,
    Long facturaId,
    Integer numeroCuota,
    BigDecimal monto,
    BigDecimal montoPagado,
    BigDecimal saldo,
    LocalDate fechaVencimiento,
    LocalDateTime fechaPagoCompleta,
    String estado
) {}
