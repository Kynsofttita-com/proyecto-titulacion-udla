package com.escuela.auth.dto;

import java.math.BigDecimal;

public record ConceptoFacturacionResponse(
        Long id,
        String nombre,
        BigDecimal montoBase,
        String descripcion,
        Boolean activo
) {}
