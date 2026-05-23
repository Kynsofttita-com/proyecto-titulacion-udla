package com.escuela.cobros.exception;

public class EstudianteNotFoundException extends RuntimeException {

    public EstudianteNotFoundException(Long id) {
        super("Estudiante con ID " + id + " no encontrado");
    }

    public EstudianteNotFoundException(String message) {
        super(message);
    }
}
