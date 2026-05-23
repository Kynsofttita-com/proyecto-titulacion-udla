package com.escuela.asignaciones.exception;

import com.escuela.common.exceptions.NegocioException;
import org.springframework.http.HttpStatus;

public class EstudianteInactivoException extends NegocioException {

    public EstudianteInactivoException(Long estudianteId) {
        super(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "ESTUDIANTE_INACTIVO",
                "Estudiante con ID " + estudianteId + " no está activo"
        );
    }
}
