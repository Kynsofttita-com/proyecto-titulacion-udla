package com.escuela.cobros.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagoResponse(
    Long id,
    Long facturaId,
    BigDecimal monto,
    LocalDateTime fechaPago,
    String metodoPago,
    String referenciaTransaccion,
    String observaciones,
    Long usuarioRegistroId,
    LocalDateTime createdAt,
    String createdBy
) {}
