package com.escuela.cobros.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateCobroRequest(
        Long estudianteId,
        BigDecimal monto,
        String tipoPago,
        String referencia,
        String estado,
        LocalDate fecha,
        String observaciones
) {}
