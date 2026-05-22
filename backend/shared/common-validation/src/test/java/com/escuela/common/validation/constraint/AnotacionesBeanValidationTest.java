package com.escuela.common.validation.constraint;

import com.escuela.common.validation.annotation.CedulaEcuador;
import com.escuela.common.validation.annotation.PlacaEcuador;
import com.escuela.common.validation.annotation.RucEcuador;
import com.escuela.common.validation.annotation.TelefonoEcuador;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica el puente JSR-380: las anotaciones aplicadas a un POJO disparan
 * el ConstraintValidator correspondiente cuando se llama al Validator.
 */
class AnotacionesBeanValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        if (factory != null) {
            factory.close();
        }
    }

    private record Persona(
            @CedulaEcuador String cedula,
            @RucEcuador String ruc,
            @PlacaEcuador String placa,
            @TelefonoEcuador String telefono
    ) {}

    @Test
    @DisplayName("Todos los campos validos -> sin violaciones")
    void todoValido() {
        Persona p = new Persona("1710034065", "1710034065001", "PCI-1234", "0991234567");
        Set<ConstraintViolation<Persona>> v = validator.validate(p);
        assertTrue(v.isEmpty(), "no debe haber violaciones, obtuvo: " + v);
    }

    @Test
    @DisplayName("Cedula invalida -> 1 violacion")
    void cedulaInvalida() {
        Persona p = new Persona("1710034066", null, null, null);
        Set<ConstraintViolation<Persona>> v = validator.validate(p);
        assertEquals(1, v.size());
        assertEquals("cedula", v.iterator().next().getPropertyPath().toString());
    }

    @Test
    @DisplayName("RUC invalido -> 1 violacion")
    void rucInvalido() {
        Persona p = new Persona(null, "1710034065002", null, null);
        Set<ConstraintViolation<Persona>> v = validator.validate(p);
        assertEquals(1, v.size());
        assertEquals("ruc", v.iterator().next().getPropertyPath().toString());
    }

    @Test
    @DisplayName("Placa invalida -> 1 violacion")
    void placaInvalida() {
        Persona p = new Persona(null, null, "ABCD-12", null);
        Set<ConstraintViolation<Persona>> v = validator.validate(p);
        assertEquals(1, v.size());
        assertEquals("placa", v.iterator().next().getPropertyPath().toString());
    }

    @Test
    @DisplayName("Telefono invalido -> 1 violacion")
    void telefonoInvalido() {
        Persona p = new Persona(null, null, null, "012345");
        Set<ConstraintViolation<Persona>> v = validator.validate(p);
        assertEquals(1, v.size());
        assertEquals("telefono", v.iterator().next().getPropertyPath().toString());
    }

    @Test
    @DisplayName("null en todos los campos -> sin violaciones (annotations no implican @NotNull)")
    void nullPasa() {
        Persona p = new Persona(null, null, null, null);
        Set<ConstraintViolation<Persona>> v = validator.validate(p);
        assertTrue(v.isEmpty());
    }
}
