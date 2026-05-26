package com.escuela.vehiculos.dto;

import java.time.LocalDate;

public record VehiculoListResponse(
        Long id,
        String placa,
        String marca,
        String modelo,
        Short anio,
        String color,
        Integer kilometraje,
        String estado,
        LocalDate soatVencimiento,
        LocalDate revisionVencimiento,
        Long categoriaLicenciaId,
        Long tipoCombustibleId
) {}
