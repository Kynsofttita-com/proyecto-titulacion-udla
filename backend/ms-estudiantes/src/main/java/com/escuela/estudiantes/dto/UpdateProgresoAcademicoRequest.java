package com.escuela.estudiantes.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;

public record UpdateProgresoAcademicoRequest(
        @Min(0) Short clasesPlaneadas,
        @Min(0) Short clasesCompletadas,
        @Min(0) Short clasesPendientes,
        @Min(0) Short clasesCanceladas,
        @DecimalMin("0.00") @DecimalMax("20.00") BigDecimal calificacionPromedio,
        Boolean aprobado
) {}
