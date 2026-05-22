package com.escuela.common.validation.annotation;

import com.escuela.common.validation.constraint.CedulaEcuadorConstraintValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca un campo {@code String} como cedula ecuatoriana valida.
 * Valida formato (10 digitos) y digito verificador (algoritmo modulo 10).
 *
 * <p>Si el valor es {@code null}, la validacion pasa (no implica @NotNull).
 * Combinar con {@code @NotBlank} si se requiere presencia.</p>
 *
 * <p>Ejemplo:</p>
 * <pre>
 *   public record CrearEstudianteRequest(
 *       {@literal @}NotBlank {@literal @}CedulaEcuador String cedula
 *   ) {}
 * </pre>
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CedulaEcuadorConstraintValidator.class)
public @interface CedulaEcuador {
    String message() default "Cedula ecuatoriana invalida";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
