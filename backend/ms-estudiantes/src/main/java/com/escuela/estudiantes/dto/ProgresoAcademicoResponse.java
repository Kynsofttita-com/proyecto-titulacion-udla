package com.escuela.estudiantes.dto;

import java.math.BigDecimal;

public record ProgresoAcademicoResponse(
        Long id,
        Long estudianteId,
        Short clasesPlaneadas,
        Short clasesCompletadas,
        Short clasesPendientes,
        Short clasesCanceladas,
        BigDecimal calificacionPromedio,
        Boolean aprobado
) {}
