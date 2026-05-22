package com.escuela.common.validation.constraint;

import com.escuela.common.validation.annotation.RucEcuador;
import com.escuela.common.validation.core.RucEcuadorValidator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RucEcuadorConstraintValidator
        implements ConstraintValidator<RucEcuador, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return RucEcuadorValidator.isValid(value);
    }
}
