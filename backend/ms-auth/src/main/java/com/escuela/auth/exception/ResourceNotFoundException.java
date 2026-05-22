package com.escuela.auth.exception;

/** Recurso no encontrado por id. Mapea a HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String recurso, Object id) {
        super(recurso + " no encontrado con id=" + id);
    }
}
