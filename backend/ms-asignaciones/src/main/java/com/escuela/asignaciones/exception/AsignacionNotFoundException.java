package com.escuela.asignaciones.exception;

public class AsignacionNotFoundException extends RuntimeException {
    public AsignacionNotFoundException(Long id) {
        super("Asignación con ID " + id + " no encontrada");
    }
}
