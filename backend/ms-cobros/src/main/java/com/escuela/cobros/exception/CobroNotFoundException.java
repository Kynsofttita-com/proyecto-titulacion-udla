package com.escuela.cobros.exception;

public class CobroNotFoundException extends RuntimeException {
    public CobroNotFoundException(Long id) {
        super("Cobro con ID " + id + " no encontrado");
    }
}
