package com.escuela.estudiantes.exception;

/**
 * Lanzada al intentar usar un email ya registrado por otro estudiante activo.
 * El handler global la mapea a HTTP 409 Conflict.
 */
public class EmailDuplicadoException extends RuntimeException {

    public EmailDuplicadoException(String email) {
        super("Ya existe un estudiante con el email " + email);
    }
}
