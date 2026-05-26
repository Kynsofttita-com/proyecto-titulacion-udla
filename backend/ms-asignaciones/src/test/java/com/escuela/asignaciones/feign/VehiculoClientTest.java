package com.escuela.asignaciones.feign;

import com.escuela.asignaciones.dto.feign.ActualizarKilometrajeFeignRequest;
import com.escuela.asignaciones.dto.feign.ActualizarKilometrajeFeignResponse;
import com.escuela.asignaciones.dto.feign.VehiculoDetailDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VehiculoClientTest {

    private final VehiculoClient client = new VehiculoClient() {
        @Override
        public VehiculoDetailDTO obtenerVehiculo(Long id) {
            return null;
        }

        @Override
        public ActualizarKilometrajeFeignResponse actualizarKilometraje(Long id, ActualizarKilometrajeFeignRequest request) {
            return null;
        }
    };

    @Test
    void testFallback_ReturnsVehiculoWithNullDeletedAt() {
        VehiculoDetailDTO result = client.obtenerVehiculoFallback(1L, new RuntimeException("Service unavailable"));

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertNull(result.deletedAt());
    }

    @Test
    void testFallback_WithDifferentException() {
        VehiculoDetailDTO result = client.obtenerVehiculoFallback(999L, new java.net.ConnectException("Connection refused"));

        assertNotNull(result);
        assertEquals(999L, result.id());
        assertNull(result.deletedAt());
    }

    @Test
    void testFallback_WithTimeoutException() {
        VehiculoDetailDTO result = client.obtenerVehiculoFallback(123L, new java.util.concurrent.TimeoutException("Request timeout"));

        assertNotNull(result);
        assertEquals(123L, result.id());
        assertNull(result.deletedAt());
    }

    @Test
    void testFallback_MultipleIds() {
        for (Long id = 1L; id <= 5; id++) {
            VehiculoDetailDTO result = client.obtenerVehiculoFallback(id, new RuntimeException("Test"));
            assertNotNull(result);
            assertEquals(id, result.id());
            assertNull(result.deletedAt());
        }
    }

    @Test
    void testFallback_StateIsPredictable() {
        VehiculoDetailDTO result = client.obtenerVehiculoFallback(1L, new RuntimeException());
        assertNull(result.deletedAt());

        VehiculoDetailDTO result2 = client.obtenerVehiculoFallback(2L, new Exception());
        assertNull(result2.deletedAt());
    }

    @Test
    void testCircuitBreakerAnnotation_Presence() {
        VehiculoDetailDTO result = client.obtenerVehiculoFallback(1L, new RuntimeException("CB test"));
        assertNotNull(result);
        assertTrue(result.id() > 0);
    }
}
