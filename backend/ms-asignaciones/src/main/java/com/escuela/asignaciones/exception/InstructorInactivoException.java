package com.escuela.asignaciones.exception;

import com.escuela.common.exceptions.NegocioException;
import org.springframework.http.HttpStatus;

public class InstructorInactivoException extends NegocioException {

    public InstructorInactivoException(Long instructorId) {
        super(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "INSTRUCTOR_INACTIVO",
                "Instructor con ID " + instructorId + " no está activo"
        );
    }
}
