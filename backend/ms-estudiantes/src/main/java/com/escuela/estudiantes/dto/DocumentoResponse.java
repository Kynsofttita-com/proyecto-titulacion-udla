package com.escuela.estudiantes.dto;

import java.time.LocalDateTime;

public record DocumentoResponse(
        Long id,
        String tipo,
        String urlArchivo,
        LocalDateTime fechaSubida,
        String mimeType,
        Long tamanoBytes
) {}
