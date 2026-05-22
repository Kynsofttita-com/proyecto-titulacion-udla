package com.escuela.common.validation.annotation;

import com.escuela.common.validation.constraint.RucEcuadorConstraintValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca un campo {@code String} como RUC ecuatoriano valido (13 digitos).
 * Soporta persona natural, sociedad publica y sociedad privada / extranjero.
 *
 * <p>Si el valor es {@code null}, la validacion pasa.</p>
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RucEcuadorConstraintValidator.class)
public @interface RucEcuador {
    String message() default "RUC ecuatoriano invalido";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
