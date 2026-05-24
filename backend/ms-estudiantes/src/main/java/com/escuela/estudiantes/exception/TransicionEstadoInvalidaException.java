package com.escuela.estudiantes.exception;

/**
 * Se lanza cuando se intenta una transicion de estado academico que no esta
 * permitida (p.ej. PRE_MATRICULADO -> COMPLETADO directo).
 */
public class TransicionEstadoInvalidaException extends RuntimeException {

    public TransicionEstadoInvalidaException(String estadoActual, String estadoNuevo) {
        super("Transicion no permitida: " + estadoActual + " -> " + estadoNuevo);
    }

    public TransicionEstadoInvalidaException(String mensaje) {
        super(mensaje);
    }
}
