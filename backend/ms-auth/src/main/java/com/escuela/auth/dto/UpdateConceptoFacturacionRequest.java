package com.escuela.auth.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateConceptoFacturacionRequest(
        @Size(max = 100) String nombre,
        @DecimalMin(value = "0.00") BigDecimal montoBase,
        String descripcion,
        Boolean activo
) {}
