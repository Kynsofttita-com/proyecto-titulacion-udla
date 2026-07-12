package com.escuela.reportes.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ParametrosHashUtilTest {

    @InjectMocks
    private ParametrosHashUtil util;

    @Test
    void testGenerarHashDesdeMap() {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("tipo", "ESTUDIANTES");
        parametros.put("año", 2026);

        String hash1 = util.generarHash(parametros);
        String hash2 = util.generarHash(parametros);

        assertNotNull(hash1);
        assertEquals(hash1, hash2);
        assertTrue(hash1.length() > 0);
    }

    @Test
    void testGenerarHashDesdeValores() {
        String hash1 = util.generarHash("TIPO", "2026", "ESTUDIANTES");
        String hash2 = util.generarHash("TIPO", "2026", "ESTUDIANTES");

        assertNotNull(hash1);
        assertEquals(hash1, hash2);
    }

    @Test
    void testHashesDistintosParaParametrosDistintos() {
        Map<String, Object> parametros1 = new HashMap<>();
        parametros1.put("tipo", "ESTUDIANTES");

        Map<String, Object> parametros2 = new HashMap<>();
        parametros2.put("tipo", "INSTRUCTORES");

        String hash1 = util.generarHash(parametros1);
        String hash2 = util.generarHash(parametros2);

        assertNotEquals(hash1, hash2);
    }

    @Test
    void testGenerarHashMapVacio() {
        String hash = util.generarHash(new HashMap<>());
        assertNotNull(hash);
        assertTrue(hash.length() > 0);
    }

    @Test
    void testGenerarHashNull() {
        String hash = util.generarHash((Map<String, Object>) null);
        assertNotNull(hash);
        assertTrue(hash.length() > 0);
    }

    @Test
    void testValidarHashCorrecto() {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("clave", "valor");

        String hash = util.generarHash(parametros);
        boolean valido = util.validarHash(parametros, hash);

        assertTrue(valido);
    }

    @Test
    void testValidarHashIncorrecto() {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("clave", "valor");

        boolean valido = util.validarHash(parametros, "hash-invalido");

        assertFalse(valido);
    }

    @Test
    void testGenerarHashConValoresNulos() {
        String hash = util.generarHash("tipo", null, "value");
        assertNotNull(hash);
        assertTrue(hash.length() > 0);
    }
}
