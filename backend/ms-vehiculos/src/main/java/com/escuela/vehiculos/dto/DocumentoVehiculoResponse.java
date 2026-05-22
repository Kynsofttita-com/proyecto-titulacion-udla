package com.escuela.vehiculos.dto;

import java.time.LocalDate;

public record DocumentoVehiculoResponse(
        Long id,
        Long vehiculoId,
        String tipo,
        String numero,
        String urlArchivo,
        LocalDate fechaEmision,
        LocalDate fechaVencimiento
) {}
