package com.escuela.instructores.dto;

import java.time.LocalDate;

public record CertificacionResponse(
        Long id,
        String tipo,
        LocalDate fechaObtencion,
        LocalDate vigenciaHasta,
        String entidadEmisora,
        String archivoUrl,
        String observaciones
) {}
