package com.escuela.common.validation.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TelefonoEcuadorValidatorTest {

    @ParameterizedTest(name = "movil valido: {0}")
    @ValueSource(strings = {"0991234567", "0987654321", "0900000000"})
    @DisplayName("Telefonos moviles validos")
    void movilValido(String telefono) {
        assertTrue(TelefonoEcuadorValidator.isValid(telefono));
        assertTrue(TelefonoEcuadorValidator.esMovil(telefono));
        assertFalse(TelefonoEcuadorValidator.esFijo(telefono));
    }

    @ParameterizedTest(name = "fijo valido: {0}")
    @ValueSource(strings = {
            "022234567", // Quito
            "032234567", // Tungurahua
            "042234567", // Guayas
            "052234567", // Manabi
            "062234567", // Imbabura
            "072234567"  // Loja
    })
    @DisplayName("Telefonos fijos validos por provincia")
    void fijoValido(String telefono) {
        assertTrue(TelefonoEcuadorValidator.isValid(telefono));
        assertTrue(TelefonoEcuadorValidator.esFijo(telefono));
        assertFalse(TelefonoEcuadorValidator.esMovil(telefono));
    }

    @ParameterizedTest(name = "invalido: {0}")
    @ValueSource(strings = {
            "022234",          // muy corto
            "0822234567",      // prefijo 08 no asignado
            "0122234567",      // prefijo 01 no asignado
            "+593987654321",   // no acepta prefijo internacional
            "abcdefghij",      // letras
            ""
    })
    @DisplayName("Formatos invalidos rechazados")
    void invalido(String telefono) {
        assertFalse(TelefonoEcuadorValidator.isValid(telefono));
    }

    @Test
    @DisplayName("null devuelve false")
    void nullDevuelveFalse() {
        assertFalse(TelefonoEcuadorValidator.isValid(null));
        assertFalse(TelefonoEcuadorValidator.esMovil(null));
        assertFalse(TelefonoEcuadorValidator.esFijo(null));
    }
}
