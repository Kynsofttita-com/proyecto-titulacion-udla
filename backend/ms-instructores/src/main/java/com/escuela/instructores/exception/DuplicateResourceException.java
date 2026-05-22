package com.escuela.instructores.exception;

public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String mensaje) {
        super(mensaje);
    }
}
