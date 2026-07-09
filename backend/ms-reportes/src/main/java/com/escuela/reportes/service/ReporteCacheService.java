package com.escuela.reportes.service;

import com.escuela.reportes.dto.ReporteOperativoResponse;
import com.escuela.reportes.util.ParametrosHashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReporteCacheService {

    private final ParametrosHashUtil hashUtil;
    private final ReporteService reporteService;

    @Cacheable(value = "reportes_cache", key = "#hashParametros", unless = "#result == null")
    public ReporteOperativoResponse obtenerReporteCacheado(
        String tipoReporte,
        String hashParametros,
        Map<String, Object> parametros
    ) {
        log.info("Cache miss - Generando reporte: {}", tipoReporte);
        return generarReporteSinCache(tipoReporte, parametros);
    }

    @Cacheable(value = "reportes_operativos", key = "#hashParametros", unless = "#result == null")
    public ReporteOperativoResponse obtenerReporteOperativosCacheado(
        String hashParametros,
        Map<String, Object> parametros
    ) {
        log.info("Cache miss - Generando reporte operativo con hash: {}", hashParametros);
        return reporteService.generarReporteEstudiantesActivos(null);
    }

    @CacheEvict(value = "reportes_cache", allEntries = true)
    public void limpiarTodoCache() {
        log.info("Limpiando todo el caché de reportes");
    }

    @CacheEvict(value = "reportes_cache", key = "#hashParametros")
    public void limpiarCacheReporte(String hashParametros) {
        log.info("Limpiando caché de reporte: {}", hashParametros);
    }

    @CacheEvict(value = "reportes_operativos", allEntries = true)
    public void limpiarCacheOperativos() {
        log.info("Limpiando caché de reportes operativos");
    }

    public String generarHashParametros(Map<String, Object> parametros) {
        return hashUtil.generarHash(parametros);
    }

    public ReporteOperativoResponse obtenerOGenerarReporte(
        String tipoReporte,
        Map<String, Object> parametros
    ) {
        String hash = generarHashParametros(parametros);
        log.info("Intentando obtener reporte del caché - Tipo: {}, Hash: {}", tipoReporte, hash);
        return obtenerReporteCacheado(tipoReporte, hash, parametros);
    }

    private ReporteOperativoResponse generarReporteSinCache(
        String tipoReporte,
        Map<String, Object> parametros
    ) {
        switch (tipoReporte.toUpperCase()) {
            case "ESTUDIANTES_ACTIVOS":
                return reporteService.generarReporteEstudiantesActivos(null);
            case "INSTRUCTORES_HORAS":
                return reporteService.generarReporteInstructoresHoras(null);
            case "VEHICULOS_SOAT":
                return reporteService.generarReporteVehiculosSoat(null);
            case "ASISTENCIA":
                return reporteService.generarReporteAsistencia(null);
            case "HORAS_ASIGNACIONES":
                return reporteService.generarReporteHorasAsignaciones(null);
            default:
                throw new IllegalArgumentException("Tipo de reporte desconocido: " + tipoReporte);
        }
    }

    public Map<String, Object> obtenerEstadisticasCache() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("tipo", "Caffeine In-Memory Cache");
        stats.put("config", "maximumSize=1000, expireAfterWrite=10m");
        stats.put("timestamp", System.currentTimeMillis());
        return stats;
    }
}
