package com.escuela.cobros.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request enviado por ms-vehiculos a ms-cobros para crear/actualizar el
 * movimiento GASTO asociado a una carga de combustible o mantenimiento.
 * <p>La cuenta destino la determina ms-cobros leyendo la configuracion
 * ({@code cuentaDefaultCombustibleId} / {@code cuentaDefaultMantenimientoId}).
 * Si no hay default configurada, la creacion devuelve 204 sin efecto y
 * ms-vehiculos loguea un warning (no bloquea la operacion en Vehiculos).
 */
public record MovimientoVehiculoRequest(
        @NotNull LocalDate fecha,

        @NotNull
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
        BigDecimal monto,

        @NotNull Long vehiculoId,

        @Size(max = 20)
        String placaVehiculo,

        Integer kilometraje,

        @Size(max = 255)
        String descripcion,

        @Size(max = 80)
        String referencia
) {}
