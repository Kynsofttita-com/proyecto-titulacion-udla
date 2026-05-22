package com.escuela.instructores.exception;

public class NoAutenticadoException extends RuntimeException {
    public NoAutenticadoException() {
        super("Autenticacion requerida");
    }
}
