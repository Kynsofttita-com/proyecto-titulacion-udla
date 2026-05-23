package com.escuela.asignaciones.exception;

import com.escuela.common.exceptions.NegocioException;
import org.springframework.http.HttpStatus;

public class InstructorNoEncontradoException extends NegocioException {

    public InstructorNoEncontradoException(Long instructorId) {
        super(
                HttpStatus.NOT_FOUND,
                "INSTRUCTOR_NO_ENCONTRADO",
                "Instructor con ID " + instructorId + " no encontrado"
        );
    }
}
