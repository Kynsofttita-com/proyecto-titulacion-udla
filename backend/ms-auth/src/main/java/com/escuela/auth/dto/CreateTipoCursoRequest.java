package com.escuela.auth.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateTipoCursoRequest(
        @NotBlank @Size(max = 100) String nombre,
        String descripcion,
        @NotNull @Min(1) Short duracionTotalHoras,
        @NotNull @DecimalMin("0.00") BigDecimal precioBase,
        Long categoriaLicenciaId,
        Boolean activo
) {}
