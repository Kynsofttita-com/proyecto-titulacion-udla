package com.escuela.reportes.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record DashboardKPIResponse(
    Map<String, Object> kpis,
    LocalDateTime generadoEn,
    Long duracionMs
) {}
