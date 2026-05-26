package com.escuela.vehiculos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record InspeccionRequest(
        @NotBlank
        @Pattern(regexp = "^(TECNICA|SOAT|INTERNA)$",
                message = "tipo debe ser TECNICA, SOAT o INTERNA")
        String tipo,

        @NotNull LocalDate fecha,

        @NotBlank
        @Pattern(regexp = "^(APROBADA|REPROBADA|CONDICIONADA)$",
                message = "resultado debe ser APROBADA, REPROBADA o CONDICIONADA")
        String resultado,

        @Size(max = 500) String archivoUrl,
        String observaciones,
        LocalDate proximaInspeccion
) {}
