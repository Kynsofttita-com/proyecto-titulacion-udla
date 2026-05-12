package com.escuela.estudiantes.exception;

/**
 * Lanzada cuando no se encuentra un Estudiante por id (o esta soft-deleted).
 * El handler global la mapea a HTTP 404 Not Found.
 */
public class EstudianteNotFoundException extends RuntimeException {

    public EstudianteNotFoundException(Long id) {
        super("Estudiante con id " + id + " no encontrado");
    }

    public EstudianteNotFoundException(String message) {
        super(message);
    }
}
