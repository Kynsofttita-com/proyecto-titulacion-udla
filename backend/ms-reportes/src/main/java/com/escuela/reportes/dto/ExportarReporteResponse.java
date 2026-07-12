package com.escuela.reportes.dto;

import java.time.LocalDateTime;

public record ExportarReporteResponse(
    String nombreArchivo,
    String formato,
    String archivoUrl,
    LocalDateTime generadoEn,
    Long tamanioBytes
) {}
