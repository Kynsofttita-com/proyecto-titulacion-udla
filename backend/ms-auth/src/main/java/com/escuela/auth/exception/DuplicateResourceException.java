package com.escuela.auth.exception;

/** Recurso ya existe con valor unico. Mapea a HTTP 409. */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String mensaje) {
        super(mensaje);
    }
}
