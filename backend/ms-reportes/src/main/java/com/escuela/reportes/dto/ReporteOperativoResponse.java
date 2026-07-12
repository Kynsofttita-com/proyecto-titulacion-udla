package com.escuela.reportes.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record ReporteOperativoResponse(
    String tipoReporte,
    Map<String, Object> datos,
    LocalDateTime generadoEn,
    Long duracionMs,
    Integer totalRegistros
) {}
