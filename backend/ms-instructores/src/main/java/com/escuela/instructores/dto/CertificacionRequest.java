package com.escuela.instructores.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CertificacionRequest(
        @NotBlank @Size(max = 100) String tipo,
        @NotNull LocalDate fechaObtencion,
        LocalDate vigenciaHasta,
        String entidadEmisora,
        @Size(max = 500) String archivoUrl,
        String observaciones
) {}
