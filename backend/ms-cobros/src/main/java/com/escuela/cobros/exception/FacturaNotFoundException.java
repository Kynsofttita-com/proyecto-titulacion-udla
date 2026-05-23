package com.escuela.cobros.exception;

public class FacturaNotFoundException extends RuntimeException {

    public FacturaNotFoundException(Long id) {
        super("Factura con ID " + id + " no encontrada");
    }

    public FacturaNotFoundException(String message) {
        super(message);
    }
}
