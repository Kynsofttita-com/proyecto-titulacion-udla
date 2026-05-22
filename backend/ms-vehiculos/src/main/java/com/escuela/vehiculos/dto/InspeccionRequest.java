package com.escuela.vehiculos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record InspeccionRequest(
        @NotBlank
        @Pattern(regexp = "^(MATRICULA|TECNOMECANICA|SOAT|OTROS)$",
                message = "tipo invalido")
        String tipo,
        @NotNull LocalDate fecha,
        @NotBlank
        @Pattern(regexp = "^(APROBADA|RECHAZADA|PENDIENTE)$", message = "resultado invalido")
        String resultado,
        @Size(max = 500) String archivoUrl,
        String observaciones,
        LocalDate proximaInspeccion
) {}
