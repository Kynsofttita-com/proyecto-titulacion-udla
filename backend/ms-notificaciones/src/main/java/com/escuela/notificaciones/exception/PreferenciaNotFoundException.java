package com.escuela.notificaciones.exception;

public class PreferenciaNotFoundException extends RuntimeException {

    public PreferenciaNotFoundException(Long usuarioId) {
        super("Preferencia de notificación para usuario " + usuarioId + " no encontrada");
    }

    public PreferenciaNotFoundException(String message) {
        super(message);
    }
}
