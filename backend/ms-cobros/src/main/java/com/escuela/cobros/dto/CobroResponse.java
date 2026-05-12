package com.escuela.cobros.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record CobroResponse(
        Long id,
        Long estudianteId,
        BigDecimal monto,
        String tipoPago,
        String estado,
        String referencia,
        LocalDate fecha,
        String observaciones,
        LocalDateTime dateCreated,
        LocalDateTime dateUpdated
) {}
