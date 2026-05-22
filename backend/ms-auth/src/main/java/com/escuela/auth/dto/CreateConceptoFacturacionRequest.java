package com.escuela.auth.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateConceptoFacturacionRequest(
        @NotBlank @Size(max = 100) String nombre,
        @NotNull @DecimalMin(value = "0.00", message = "Monto no puede ser negativo") BigDecimal montoBase,
        String descripcion,
        Boolean activo
) {}
