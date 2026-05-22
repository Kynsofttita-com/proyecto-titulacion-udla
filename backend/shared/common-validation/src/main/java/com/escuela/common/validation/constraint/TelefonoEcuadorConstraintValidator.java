package com.escuela.common.validation.constraint;

import com.escuela.common.validation.annotation.TelefonoEcuador;
import com.escuela.common.validation.core.TelefonoEcuadorValidator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TelefonoEcuadorConstraintValidator
        implements ConstraintValidator<TelefonoEcuador, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return TelefonoEcuadorValidator.isValid(value);
    }
}
