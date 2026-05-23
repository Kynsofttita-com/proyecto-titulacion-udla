package com.escuela.cobros.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record FacturaListResponse(
    Long id,
    String numeroFactura,
    Long estudianteId,
    BigDecimal montoOriginal,
    BigDecimal montoPagado,
    BigDecimal saldo,
    String estado,
    LocalDate fechaEmision,
    LocalDateTime createdAt
) {}
