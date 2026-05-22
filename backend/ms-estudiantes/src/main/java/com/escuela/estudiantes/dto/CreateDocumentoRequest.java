package com.escuela.estudiantes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateDocumentoRequest(
        @NotBlank @Size(max = 30) String tipo,
        @NotBlank @Size(max = 500) String urlArchivo,
        @Size(max = 100) String mimeType,
        Long tamanoBytes
) {}
