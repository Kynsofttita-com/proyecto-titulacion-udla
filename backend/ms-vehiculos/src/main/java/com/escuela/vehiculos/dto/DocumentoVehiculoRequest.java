package com.escuela.vehiculos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record DocumentoVehiculoRequest(
        @NotBlank @Size(max = 50) String tipo,
        @Size(max = 100) String numero,
        @NotBlank @Size(max = 500) String urlArchivo,
        LocalDate fechaEmision,
        LocalDate fechaVencimiento
) {}
