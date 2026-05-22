package com.escuela.auth.exception;

/** Usuario sin rol suficiente para la operacion. Mapea a HTTP 403. */
public class SinPermisoException extends RuntimeException {
    public SinPermisoException() {
        super("Permisos insuficientes para esta operacion");
    }
}
