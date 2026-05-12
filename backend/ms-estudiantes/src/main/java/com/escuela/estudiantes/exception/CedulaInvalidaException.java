package com.escuela.estudiantes.exception;

/**
 * Lanzada cuando una cedula tiene formato correcto (10 digitos) pero falla
 * la validacion del digito verificador del algoritmo ecuatoriano.
 * El handler global la mapea a HTTP 400 Bad Request.
 */
public class CedulaInvalidaException extends RuntimeException {

    public CedulaInvalidaException(String cedula) {
        super("La cedula " + cedula + " no es valida (digito verificador incorrecto)");
    }
}
