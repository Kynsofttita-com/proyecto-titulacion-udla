package com.escuela.cobros.exception;

public class PagoNotFoundException extends RuntimeException {

    public PagoNotFoundException(Long id) {
        super("Pago con ID " + id + " no encontrado");
    }

    public PagoNotFoundException(String message) {
        super(message);
    }
}
