package com.escuela.vehiculos.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateVehiculoRequest(
        @Size(min = 1, max = 50) String marca,
        @Size(min = 1, max = 50) String modelo,

        @Min(value = 1990) @Max(value = 2050) Short anio,

        @Size(max = 30) String color,

        @Min(value = 0) Integer kilometraje,

        @Pattern(regexp = "^(ACTIVO|MANTENIMIENTO|FUERA_SERVICIO)$",
                message = "estado debe ser ACTIVO, MANTENIMIENTO o FUERA_SERVICIO")
        String estado,

        LocalDate soatVencimiento,
        LocalDate revisionVencimiento,
        LocalDate fechaCompra,

        @DecimalMin(value = "0.0") BigDecimal valorCompra,

        Long categoriaLicenciaId,
        Long tipoCombustibleId,

        @Size(max = 50) String numeroMotor,
        @Size(max = 50) String numeroChasis,

        @Min(1) @Max(100) Short capacidadPasajeros,

        String observaciones
) {}
