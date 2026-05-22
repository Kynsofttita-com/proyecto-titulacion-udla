package com.escuela.common.validation.annotation;

import com.escuela.common.validation.constraint.PlacaEcuadorConstraintValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca un campo {@code String} como placa vehicular ecuatoriana valida.
 * Acepta formato particular (ABC-1234) y comercial (AB-1234A).
 *
 * <p>Si el valor es {@code null}, la validacion pasa.</p>
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PlacaEcuadorConstraintValidator.class)
public @interface PlacaEcuador {
    String message() default "Placa vehicular invalida (formatos: ABC-1234 o AB-1234A)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
