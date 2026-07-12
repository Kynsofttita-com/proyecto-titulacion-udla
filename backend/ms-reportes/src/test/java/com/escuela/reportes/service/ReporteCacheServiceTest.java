package com.escuela.reportes.service;

import com.escuela.reportes.dto.ReporteOperativoResponse;
import com.escuela.reportes.util.ParametrosHashUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReporteCacheServiceTest {

    @Mock
    private ParametrosHashUtil hashUtil;

    @Mock
    private ReporteService reporteService;

    @InjectMocks
    private ReporteCacheService service;

    @Test
    void testGenerarHashParametros() {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("tipo", "ESTUDIANTES");
        parametros.put("año", 2026);

        when(hashUtil.generarHash(parametros)).thenReturn("test-hash-123");

        String hash = service.generarHashParametros(parametros);

        assertNotNull(hash);
        assertEquals("test-hash-123", hash);
        verify(hashUtil, times(1)).generarHash(parametros);
    }

    @Test
    void testLimpiarTodoCache() {
        assertDoesNotThrow(() -> service.limpiarTodoCache());
    }

    @Test
    void testLimpiarCacheReporte() {
        String hash = "test-hash";
        assertDoesNotThrow(() -> service.limpiarCacheReporte(hash));
    }

    @Test
    void testLimpiarCacheOperativos() {
        assertDoesNotThrow(() -> service.limpiarCacheOperativos());
    }

    @Test
    void testObtenerEstadisticasCache() {
        Map<String, Object> stats = service.obtenerEstadisticasCache();

        assertNotNull(stats);
        assertTrue(stats.containsKey("tipo"));
        assertTrue(stats.containsKey("config"));
        assertTrue(stats.containsKey("timestamp"));
        assertEquals("Caffeine In-Memory Cache", stats.get("tipo"));
    }

    @Test
    void testObtenerOGenerarReporte() {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("tipo", "ESTUDIANTES_ACTIVOS");

        ReporteOperativoResponse mockReporte = new ReporteOperativoResponse(
            "ESTUDIANTES_ACTIVOS",
            new HashMap<>(),
            LocalDateTime.now(),
            100L,
            50
        );

        when(hashUtil.generarHash(parametros)).thenReturn("hash-123");
        when(reporteService.generarReporteEstudiantesActivos(null)).thenReturn(mockReporte);

        ReporteOperativoResponse resultado = service.obtenerOGenerarReporte("ESTUDIANTES_ACTIVOS", parametros);

        assertNotNull(resultado);
        assertEquals("ESTUDIANTES_ACTIVOS", resultado.tipoReporte());
    }

    @Test
    void testParametrosVacios() {
        when(hashUtil.generarHash(new HashMap<>())).thenReturn("empty-hash");

        String hash = service.generarHashParametros(new HashMap<>());

        assertNotNull(hash);
        assertEquals("empty-hash", hash);
    }

    @Test
    void testTiposReporteDiferentes() {
        ReporteOperativoResponse mockReporte = new ReporteOperativoResponse(
            "TEST",
            new HashMap<>(),
            LocalDateTime.now(),
            50L,
            100
        );

        when(reporteService.generarReporteEstudiantesActivos(null)).thenReturn(mockReporte);
        when(reporteService.generarReporteInstructoresHoras(null)).thenReturn(mockReporte);

        assertDoesNotThrow(() -> service.obtenerOGenerarReporte("ESTUDIANTES_ACTIVOS", new HashMap<>()));
        assertDoesNotThrow(() -> service.obtenerOGenerarReporte("INSTRUCTORES_HORAS", new HashMap<>()));
    }

    @Test
    void testTipoReporteDesconocido() {
        Map<String, Object> parametros = new HashMap<>();
        when(hashUtil.generarHash(parametros)).thenReturn("hash");

        assertThrows(IllegalArgumentException.class, () ->
            service.obtenerOGenerarReporte("TIPO_INEXISTENTE", parametros)
        );
    }
}
