package com.escuela.vehiculos.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record VehiculoResponse(
        Long id,
        String placa,
        String marca,
        String modelo,
        Short año,
        String vin,
        String color,
        Long kilometraje,
        LocalDate fechaMantenimiento,
        LocalDate fechaInspeccion,
        String observaciones,
        LocalDateTime dateCreated,
        LocalDateTime dateUpdated
) {}
