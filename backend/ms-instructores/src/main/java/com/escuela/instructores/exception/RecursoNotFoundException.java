package com.escuela.instructores.exception;

public class RecursoNotFoundException extends RuntimeException {
    public RecursoNotFoundException(String recurso, Object id) {
        super(recurso + " no encontrado con id=" + id);
    }
}
