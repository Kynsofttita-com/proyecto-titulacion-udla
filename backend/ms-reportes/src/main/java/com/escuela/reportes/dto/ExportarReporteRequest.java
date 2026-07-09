package com.escuela.reportes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ExportarReporteRequest(
    @NotNull Long reporteId,
    @NotBlank String formato
) {}
