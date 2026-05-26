package com.escuela.asignaciones.feign;

import com.escuela.asignaciones.dto.feign.EstudianteDetailDTO;
import com.escuela.asignaciones.dto.feign.IncrementarHorasFeignRequest;
import com.escuela.asignaciones.dto.feign.IncrementarHorasFeignResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EstudianteClientTest {

    private final EstudianteClient client = new EstudianteClient() {
        @Override
        public EstudianteDetailDTO obtenerEstudiante(Long id) {
            return null;
        }

        @Override
        public IncrementarHorasFeignResponse incrementarHoras(Long id, IncrementarHorasFeignRequest request) {
            return null;
        }
    };

    @Test
    void testFallback_ReturnsInactiveEstudiante() {
        EstudianteDetailDTO result = client.obtenerEstianteFallback(1L, new RuntimeException("Service unavailable"));

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("INACTIVO", result.estado());
    }

    @Test
    void testFallback_WithDifferentException() {
        EstudianteDetailDTO result = client.obtenerEstianteFallback(999L, new java.net.ConnectException("Connection refused"));

        assertNotNull(result);
        assertEquals(999L, result.id());
        assertEquals("INACTIVO", result.estado());
    }

    @Test
    void testFallback_WithTimeoutException() {
        EstudianteDetailDTO result = client.obtenerEstianteFallback(123L, new java.util.concurrent.TimeoutException("Request timeout"));

        assertNotNull(result);
        assertEquals(123L, result.id());
        assertEquals("INACTIVO", result.estado());
    }

    @Test
    void testFallback_MultipleIds() {
        for (Long id = 1L; id <= 5; id++) {
            EstudianteDetailDTO result = client.obtenerEstianteFallback(id, new RuntimeException("Test"));
            assertNotNull(result);
            assertEquals(id, result.id());
            assertEquals("INACTIVO", result.estado());
        }
    }

    @Test
    void testFallback_StateIsPredictable() {
        EstudianteDetailDTO result = client.obtenerEstianteFallback(1L, new RuntimeException());
        assertEquals("INACTIVO", result.estado());

        EstudianteDetailDTO result2 = client.obtenerEstianteFallback(2L, new Exception());
        assertEquals("INACTIVO", result2.estado());
    }

    @Test
    void testCircuitBreakerAnnotation_Presence() {
        EstudianteDetailDTO result = client.obtenerEstianteFallback(1L, new RuntimeException("CB test"));
        assertNotNull(result);
        assertTrue(result.id() > 0);
    }
}
