package com.escuela.estudiantes.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CedulaValidator")
class CedulaValidatorTest {

    @ParameterizedTest(name = "{0} es valida")
    @ValueSource(strings = {
            "1710034065",   // Pichincha
            "1700000001",   // Pichincha (sintetica)
            "0900000001",   // Guayas (sintetica)
            "2400000002"    // Galapagos (sintetica)
    })
    void cedulaValida(String cedula) {
        assertThat(CedulaValidator.isValid(cedula)).isTrue();
    }

    @ParameterizedTest(name = "{0} es invalida")
    @ValueSource(strings = {
            "1710034066",   // digito verificador incorrecto
            "1700000002",   // digito verificador incorrecto
            "0000000000",   // provincia 00 invalida
            "2500000000",   // provincia 25 inexistente
            "1760000000",   // tercer digito 6 (publica, no natural)
            "171003406",    // 9 digitos
            "17100340661",  // 11 digitos
            "170003406A"    // contiene letra
    })
    void cedulaInvalida(String cedula) {
        assertThat(CedulaValidator.isValid(cedula)).isFalse();
    }

    @ParameterizedTest
    @NullSource
    void cedulaNullEsInvalida(String cedula) {
        assertThat(CedulaValidator.isValid(cedula)).isFalse();
    }
}
