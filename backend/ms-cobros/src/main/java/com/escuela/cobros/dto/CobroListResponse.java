package com.escuela.cobros.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record CobroListResponse(
        Long id,
        Long estudianteId,
        BigDecimal monto,
        String tipoPago,
        String estado,
        LocalDate fecha,
        LocalDateTime dateCreated
) {}
