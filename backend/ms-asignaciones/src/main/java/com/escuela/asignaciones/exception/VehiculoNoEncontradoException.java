package com.escuela.asignaciones.exception;

import com.escuela.common.exceptions.NegocioException;
import org.springframework.http.HttpStatus;

public class VehiculoNoEncontradoException extends NegocioException {

    public VehiculoNoEncontradoException(Long vehiculoId) {
        super(
                HttpStatus.NOT_FOUND,
                "VEHICULO_NO_ENCONTRADO",
                "Vehículo con ID " + vehiculoId + " no encontrado"
        );
    }
}
