package com.escuela.cobros.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record EstadoCuentaResponse(
    Long estudianteId,
    BigDecimal saldoPendiente,
    BigDecimal saldoPagado,
    BigDecimal saldoVencido,
    Integer cantidadFacturasPendientes,
    Integer cantidadFacturasPagadas,
    Integer cantidadFacturasVencidas,
    LocalDateTime ultimaActividad,
    List<FacturaListResponse> facturasActivas
) {}
