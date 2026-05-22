package com.escuela.common.validation.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CedulaEcuadorValidatorTest {

    @ParameterizedTest(name = "cedula valida: {0}")
    @ValueSource(strings = {
            "1710034065", // Pichincha
            "0926687856", // Guayas
            "1758581704", // Pichincha
            "1102675327", // Loja
            "0100123454"  // Azuay
    })
    @DisplayName("Cedulas validas")
    void cedulasValidas(String cedula) {
        assertTrue(CedulaEcuadorValidator.isValid(cedula));
    }

    @ParameterizedTest(name = "cedula invalida: {0}")
    @ValueSource(strings = {
            "1710034066", // digito verificador incorrecto
            "1710034064", // digito verificador incorrecto
            "0000000000", // provincia 00 invalida
            "2510034065", // provincia 25 invalida
            "1760034065"  // tercer digito 6 invalido para persona natural
    })
    @DisplayName("Cedulas con digito verificador o provincia incorrectos")
    void cedulasInvalidas(String cedula) {
        assertFalse(CedulaEcuadorValidator.isValid(cedula));
    }

    @ParameterizedTest(name = "formato invalido: {0}")
    @ValueSource(strings = {
            "",
            "12345",       // muy corta
            "12345678901", // muy larga
            "171003406A"   // contiene letra
    })
    @DisplayName("Formatos incorrectos rechazados")
    void formatosInvalidos(String cedula) {
        assertFalse(CedulaEcuadorValidator.isValid(cedula));
    }

    @org.junit.jupiter.api.Test
    @DisplayName("null devuelve false")
    void nullDevuelveFalse() {
        assertFalse(CedulaEcuadorValidator.isValid(null));
    }
}
