package com.escuela.notificaciones.exception;

public class PlantillaNotFoundException extends RuntimeException {

    public PlantillaNotFoundException(Long id) {
        super("Plantilla con ID " + id + " no encontrada");
    }

    public PlantillaNotFoundException(String codigo) {
        super("Plantilla con código '" + codigo + "' no encontrada");
    }

}
