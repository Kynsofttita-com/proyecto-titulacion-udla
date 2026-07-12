package com.escuela.notificaciones.exception;

public class NotificacionNotFoundException extends RuntimeException {

    public NotificacionNotFoundException(Long id) {
        super("Notificación con id " + id + " no encontrada");
    }

    public NotificacionNotFoundException(String message) {
        super(message);
    }
}
