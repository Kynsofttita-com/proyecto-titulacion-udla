package com.escuela.common.validation.annotation;

import com.escuela.common.validation.constraint.TelefonoEcuadorConstraintValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca un campo {@code String} como telefono ecuatoriano valido.
 * Acepta movil (09XXXXXXXX) y fijo (02XXXXXXX a 07XXXXXXX).
 *
 * <p>Si el valor es {@code null}, la validacion pasa.</p>
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TelefonoEcuadorConstraintValidator.class)
public @interface TelefonoEcuador {
    String message() default "Telefono ecuatoriano invalido";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
