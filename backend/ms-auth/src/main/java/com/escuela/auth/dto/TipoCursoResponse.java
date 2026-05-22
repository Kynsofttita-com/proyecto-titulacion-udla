package com.escuela.auth.dto;

import java.math.BigDecimal;

public record TipoCursoResponse(
        Long id,
        String nombre,
        String descripcion,
        Short duracionTotalHoras,
        BigDecimal precioBase,
        Long categoriaLicenciaId,
        String categoriaLicenciaCodigo,
        Boolean activo
) {}
