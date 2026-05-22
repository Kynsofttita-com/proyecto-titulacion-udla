package com.escuela.common.validation.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlacaEcuadorValidatorTest {

    @ParameterizedTest(name = "placa particular valida: {0}")
    @ValueSource(strings = {"PCI-1234", "ABC-0001", "ZZZ-9999"})
    @DisplayName("Placas particulares (3 letras + 4 digitos)")
    void placaParticularValida(String placa) {
        assertTrue(PlacaEcuadorValidator.isValid(placa));
    }

    @ParameterizedTest(name = "placa comercial valida: {0}")
    @ValueSource(strings = {"AB-1234C", "GP-0001Z", "PI-9876A"})
    @DisplayName("Placas comerciales (2 letras + 4 digitos + 1 letra)")
    void placaComercialValida(String placa) {
        assertTrue(PlacaEcuadorValidator.isValid(placa));
    }

    @ParameterizedTest(name = "placa invalida: {0}")
    @ValueSource(strings = {
            "abc-1234",     // minusculas
            "AB-12345",     // 5 digitos
            "ABCD-1234",    // 4 letras
            "AB1234",       // sin guion
            "AB-12-34",     // doble guion
            "12-ABCD-12",
            ""
    })
    @DisplayName("Formatos invalidos rechazados")
    void placaInvalida(String placa) {
        assertFalse(PlacaEcuadorValidator.isValid(placa));
    }

    @org.junit.jupiter.api.Test
    @DisplayName("null devuelve false")
    void nullDevuelveFalse() {
        assertFalse(PlacaEcuadorValidator.isValid(null));
    }
}
