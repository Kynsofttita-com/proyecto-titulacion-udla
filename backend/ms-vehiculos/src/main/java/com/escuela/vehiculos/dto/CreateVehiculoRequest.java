package com.escuela.vehiculos.dto;

import com.escuela.common.validation.annotation.PlacaEcuador;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record CreateVehiculoRequest(
        @NotBlank(message = "Placa requerida")
        @PlacaEcuador
        String placa,

        @NotBlank(message = "Marca requerida")
        String marca,

        @NotBlank(message = "Modelo requerido")
        String modelo,

        @Min(value = 1900, message = "Año debe ser válido")
        @Max(value = 2100, message = "Año debe ser válido")
        Short año,

        @NotBlank(message = "VIN requerido")
        String vin,

        String color,

        @Min(value = 0, message = "Kilometraje no puede ser negativo")
        Long kilometraje,

        LocalDate fechaMantenimiento,
        LocalDate fechaInspeccion,
        String observaciones
) {}
