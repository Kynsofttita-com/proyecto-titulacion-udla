package com.escuela.vehiculos.dto;

import java.time.LocalDate;

public record InspeccionResponse(
        Long id,
        Long vehiculoId,
        String tipo,
        LocalDate fecha,
        String resultado,
        String archivoUrl,
        String observaciones,
        LocalDate proximaInspeccion
) {}
