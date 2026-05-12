package com.escuela.estudiantes.exception;

/**
 * Lanzada al intentar crear un Estudiante con una cedula que ya existe
 * (entre los no eliminados). El handler global la mapea a HTTP 409 Conflict.
 */
public class CedulaDuplicadaException extends RuntimeException {

    public CedulaDuplicadaException(String cedula) {
        super("Ya existe un estudiante con la cedula " + cedula);
    }
}
