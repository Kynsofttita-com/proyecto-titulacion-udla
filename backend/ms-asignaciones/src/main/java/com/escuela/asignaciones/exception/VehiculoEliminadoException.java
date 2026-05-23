package com.escuela.asignaciones.exception;

import com.escuela.common.exceptions.NegocioException;
import org.springframework.http.HttpStatus;

public class VehiculoEliminadoException extends NegocioException {

    public VehiculoEliminadoException(Long vehiculoId) {
        super(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "VEHICULO_ELIMINADO",
                "Vehículo con ID " + vehiculoId + " está eliminado"
        );
    }
}
