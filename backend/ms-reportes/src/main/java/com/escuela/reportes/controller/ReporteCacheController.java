package com.escuela.reportes.controller;

import com.escuela.reportes.service.ReporteCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/reportes/cache")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ReporteCacheController {

    private final ReporteCacheService cacheService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasCache() {
        return ResponseEntity.ok(cacheService.obtenerEstadisticasCache());
    }

    @DeleteMapping
    public ResponseEntity<Map<String, String>> limpiarTodoCache() {
        cacheService.limpiarTodoCache();
        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Caché de reportes limpiado exitosamente");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{hashParametros}")
    public ResponseEntity<Map<String, String>> limpiarReporteEspecifico(
        @PathVariable String hashParametros
    ) {
        cacheService.limpiarCacheReporte(hashParametros);
        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Caché específico limpiado");
        response.put("hash", hashParametros);
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/operativos/limpiar")
    public ResponseEntity<Map<String, String>> limpiarCacheOperativos() {
        cacheService.limpiarCacheOperativos();
        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Caché de reportes operativos limpiado");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/generar-hash")
    public ResponseEntity<Map<String, String>> generarHashParametros(
        @RequestBody Map<String, Object> parametros
    ) {
        String hash = cacheService.generarHashParametros(parametros);
        Map<String, String> response = new HashMap<>();
        response.put("hash", hash);
        response.put("parametros", parametros.toString());
        return ResponseEntity.ok(response);
    }
}
