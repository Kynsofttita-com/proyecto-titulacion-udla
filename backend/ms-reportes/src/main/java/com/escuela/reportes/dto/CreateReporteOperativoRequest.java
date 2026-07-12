package com.escuela.reportes.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.Map;

public record CreateReporteOperativoRequest(
    @NotBlank String tipoReporte,
    LocalDate desde,
    LocalDate hasta,
    Map<String, Object> filtros
) {}
