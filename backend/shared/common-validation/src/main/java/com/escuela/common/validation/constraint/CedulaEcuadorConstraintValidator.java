package com.escuela.common.validation.constraint;

import com.escuela.common.validation.annotation.CedulaEcuador;
import com.escuela.common.validation.core.CedulaEcuadorValidator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CedulaEcuadorConstraintValidator
        implements ConstraintValidator<CedulaEcuador, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // null delega en @NotNull / @NotBlank
        }
        return CedulaEcuadorValidator.isValid(value);
    }
}
