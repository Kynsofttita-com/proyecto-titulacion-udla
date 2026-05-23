package com.escuela.cobros.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record FacturaResponse(
    Long id,
    String numeroFactura,
    Long estudianteId,
    Long conceptoFacturacionId,
    BigDecimal montoOriginal,
    BigDecimal montoPagado,
    BigDecimal saldo,
    String estado,
    LocalDate fechaEmision,
    LocalDate fechaVencimiento,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    String createdBy,
    String updatedBy
) {}
