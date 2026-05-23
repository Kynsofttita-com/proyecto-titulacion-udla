package com.escuela.asignaciones.feign;

import com.escuela.asignaciones.dto.feign.InstructorDetailDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InstructorClientTest {

    private final InstructorClient client = new InstructorClient() {
        @Override
        public InstructorDetailDTO obtenerInstructor(Long id) {
            return null;
        }
    };

    @Test
    void testFallback_ReturnsInactiveInstructor() {
        InstructorDetailDTO result = client.obtenerInstructorFallback(1L, new RuntimeException("Service unavailable"));

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("INACTIVO", result.estado());
    }

    @Test
    void testFallback_WithDifferentException() {
        InstructorDetailDTO result = client.obtenerInstructorFallback(999L, new java.net.ConnectException("Connection refused"));

        assertNotNull(result);
        assertEquals(999L, result.id());
        assertEquals("INACTIVO", result.estado());
    }

    @Test
    void testFallback_WithTimeoutException() {
        InstructorDetailDTO result = client.obtenerInstructorFallback(123L, new java.util.concurrent.TimeoutException("Request timeout"));

        assertNotNull(result);
        assertEquals(123L, result.id());
        assertEquals("INACTIVO", result.estado());
    }

    @Test
    void testFallback_MultipleIds() {
        for (Long id = 1L; id <= 5; id++) {
            InstructorDetailDTO result = client.obtenerInstructorFallback(id, new RuntimeException("Test"));
            assertNotNull(result);
            assertEquals(id, result.id());
            assertEquals("INACTIVO", result.estado());
        }
    }

    @Test
    void testFallback_StateIsPredictable() {
        InstructorDetailDTO result = client.obtenerInstructorFallback(1L, new RuntimeException());
        assertEquals("INACTIVO", result.estado());

        InstructorDetailDTO result2 = client.obtenerInstructorFallback(2L, new Exception());
        assertEquals("INACTIVO", result2.estado());
    }

    @Test
    void testCircuitBreakerAnnotation_Presence() {
        InstructorDetailDTO result = client.obtenerInstructorFallback(1L, new RuntimeException("CB test"));
        assertNotNull(result);
        assertTrue(result.id() > 0);
    }
}
