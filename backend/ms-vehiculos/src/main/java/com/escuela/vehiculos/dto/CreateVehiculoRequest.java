package com.escuela.vehiculos.dto;

import com.escuela.common.validation.annotation.PlacaEcuador;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateVehiculoRequest(
        @NotBlank(message = "Placa requerida")
        @PlacaEcuador
        String placa,

        @NotBlank(message = "Marca requerida")
        @Size(min = 1, max = 50, message = "Marca entre 1 y 50 caracteres")
        String marca,

        @NotBlank(message = "Modelo requerido")
        @Size(min = 1, max = 50, message = "Modelo entre 1 y 50 caracteres")
        String modelo,

        @NotNull(message = "Año requerido")
        @Min(value = 1990, message = "Año debe ser >= 1990")
        @Max(value = 2050, message = "Año debe ser <= 2050")
        Short anio,

        @Size(min = 17, max = 17, message = "VIN debe tener 17 caracteres")
        String vin,

        @Size(max = 30) String color,

        @Min(value = 0, message = "Kilometraje no puede ser negativo")
        Integer kilometraje,

        @Pattern(regexp = "^(ACTIVO|MANTENIMIENTO|FUERA_SERVICIO)$",
                message = "estado debe ser ACTIVO, MANTENIMIENTO o FUERA_SERVICIO")
        String estado,

        LocalDate soatVencimiento,
        LocalDate revisionVencimiento,
        LocalDate fechaCompra,

        @DecimalMin(value = "0.0", message = "Valor de compra debe ser >= 0")
        BigDecimal valorCompra,

        Long categoriaLicenciaId,
        Long tipoCombustibleId,

        @Size(max = 50) String numeroMotor,
        @Size(max = 50) String numeroChasis,

        @Min(value = 1, message = "Capacidad debe ser >= 1")
        @Max(value = 100, message = "Capacidad debe ser <= 100")
        Short capacidadPasajeros,

        String observaciones
) {}
