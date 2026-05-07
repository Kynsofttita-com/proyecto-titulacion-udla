package com.escuela.auth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test: verifica que el contexto de Spring carga correctamente.
 * Si este test pasa, la configuración base del microservicio es válida.
 */
@SpringBootTest
@ActiveProfiles("test")
class AuthApplicationTests {

    @Test
    void contextLoads() {
        // Si el contexto falla al cargar, este test fallará automáticamente.
    }
}
