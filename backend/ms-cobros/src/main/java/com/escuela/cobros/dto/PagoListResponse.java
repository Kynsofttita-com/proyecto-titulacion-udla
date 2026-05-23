package com.escuela.cobros.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagoListResponse(
    Long id,
    Long facturaId,
    BigDecimal monto,
    LocalDateTime fechaPago,
    String metodoPago,
    String referenciaTransaccion
) {}
