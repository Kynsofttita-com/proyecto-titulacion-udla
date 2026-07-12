package com.escuela.reportes.listener;

import com.escuela.reportes.service.ReporteCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReporteCleanupListener {

    private final ReporteCacheService cacheService;

    @Scheduled(cron = "0 0 2 * * ?") // 02:00 cada día (UTC)
    public void limpiarCacheDiario() {
        log.info("=== INICIANDO LIMPIEZA DIARIA DE CACHÉ ===");
        try {
            cacheService.limpiarTodoCache();
            log.info("=== LIMPIEZA DIARIA COMPLETADA EXITOSAMENTE ===");
        } catch (Exception ex) {
            log.error("Error durante limpieza diaria de caché: {}", ex.getMessage(), ex);
        }
    }

    @Scheduled(cron = "0 */6 * * * ?") // Cada 6 horas
    public void limpiarCacheOperativosCadaSeis() {
        log.info("Limpiando caché de reportes operativos (cada 6 horas)");
        try {
            cacheService.limpiarCacheOperativos();
        } catch (Exception ex) {
            log.error("Error limpiando caché de operativos: {}", ex.getMessage(), ex);
        }
    }

    @Scheduled(initialDelay = 60000, fixedDelay = 3600000) // Cada hora
    public void verificarEspacioCache() {
        log.debug("Verificación periódica de estado del caché");
        try {
            var stats = cacheService.obtenerEstadisticasCache();
            log.debug("Stats cache: {}", stats);
        } catch (Exception ex) {
            log.error("Error verificando stats cache: {}", ex.getMessage(), ex);
        }
    }
}
