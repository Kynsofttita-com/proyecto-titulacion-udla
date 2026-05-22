package com.escuela.estudiantes.exception;

/** Sub-recurso (Documento, ContactoEmergencia, Asistencia, Progreso) no encontrado. */
public class RecursoNotFoundException extends RuntimeException {
    public RecursoNotFoundException(String recurso, Object id) {
        super(recurso + " no encontrado con id=" + id);
    }
}
