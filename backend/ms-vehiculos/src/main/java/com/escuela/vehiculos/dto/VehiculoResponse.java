package com.escuela.vehiculos.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record VehiculoResponse(
        Long id,
        String placa,
        String marca,
        String modelo,
        Short anio,
        String vin,
        String color,
        Integer kilometraje,
        String estado,
        LocalDate soatVencimiento,
        LocalDate revisionVencimiento,
        LocalDate fechaCompra,
        BigDecimal valorCompra,
        Long categoriaLicenciaId,
        Long tipoCombustibleId,
        String numeroMotor,
        String numeroChasis,
        Short capacidadPasajeros,
        String observaciones,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
