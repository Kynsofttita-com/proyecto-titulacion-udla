package com.escuela.instructores.exception;

public class SinPermisoException extends RuntimeException {
    public SinPermisoException() {
        super("Permisos insuficientes para esta operacion");
    }
}
