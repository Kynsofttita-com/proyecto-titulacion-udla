package com.escuela.instructores;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test: verifica que el contexto de Spring carga correctamente.
 */
@SpringBootTest
@ActiveProfiles("test")
class InstructoresApplicationTests {

    @Test
    void contextLoads() {
        // Si el contexto falla al cargar, este test fallará automáticamente.
    }
}
