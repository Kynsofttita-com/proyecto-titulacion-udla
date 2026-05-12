package com.escuela.vehiculos.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record VehiculoListResponse(
        Long id,
        String placa,
        String marca,
        String modelo,
        Short año,
        Long kilometraje,
        LocalDate fechaMantenimiento,
        LocalDateTime dateCreated
) {}
