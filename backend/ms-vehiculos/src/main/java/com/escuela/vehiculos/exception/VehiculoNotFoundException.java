package com.escuela.vehiculos.exception;

public class VehiculoNotFoundException extends RuntimeException {
    public VehiculoNotFoundException(Long id) {
        super("Vehículo con ID " + id + " no encontrado");
    }

    public VehiculoNotFoundException(String message) {
        super(message);
    }
}
