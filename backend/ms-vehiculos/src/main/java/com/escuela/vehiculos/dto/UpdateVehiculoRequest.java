package com.escuela.vehiculos.dto;

import java.time.LocalDate;

public record UpdateVehiculoRequest(
        String marca,
        String modelo,
        Short año,
        String color,
        Long kilometraje,
        LocalDate fechaMantenimiento,
        LocalDate fechaInspeccion,
        String observaciones
) {}
