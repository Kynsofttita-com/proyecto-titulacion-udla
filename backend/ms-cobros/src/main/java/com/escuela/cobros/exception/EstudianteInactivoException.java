package com.escuela.cobros.exception;

public class EstudianteInactivoException extends RuntimeException {

    public EstudianteInactivoException(Long estudianteId) {
        super("Estudiante con ID " + estudianteId + " se encuentra inactivo");
    }

    public EstudianteInactivoException(String message) {
        super(message);
    }
}
