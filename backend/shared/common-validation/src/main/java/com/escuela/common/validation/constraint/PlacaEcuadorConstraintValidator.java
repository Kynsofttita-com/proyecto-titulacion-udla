package com.escuela.common.validation.constraint;

import com.escuela.common.validation.annotation.PlacaEcuador;
import com.escuela.common.validation.core.PlacaEcuadorValidator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PlacaEcuadorConstraintValidator
        implements ConstraintValidator<PlacaEcuador, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return PlacaEcuadorValidator.isValid(value);
    }
}
