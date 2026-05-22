package com.escuela.auth.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateTipoCursoRequest(
        @Size(max = 100) String nombre,
        String descripcion,
        @Min(1) Short duracionTotalHoras,
        @DecimalMin("0.00") BigDecimal precioBase,
        Long categoriaLicenciaId,
        Boolean activo
) {}
