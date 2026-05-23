package com.escuela.asignaciones.exception;

import com.escuela.common.exceptions.NegocioException;
import org.springframework.http.HttpStatus;

public class EstudianteNoEncontradoException extends NegocioException {

    public EstudianteNoEncontradoException(Long estudianteId) {
        super(
                HttpStatus.NOT_FOUND,
                "ESTUDIANTE_NO_ENCONTRADO",
                "Estudiante con ID " + estudianteId + " no encontrado"
        );
    }
}
