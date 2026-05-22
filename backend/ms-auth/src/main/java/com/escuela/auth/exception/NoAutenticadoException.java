package com.escuela.auth.exception;

/** Request sin header X-User-Email. Mapea a HTTP 401. */
public class NoAutenticadoException extends RuntimeException {
    public NoAutenticadoException() {
        super("Autenticacion requerida");
    }
}
