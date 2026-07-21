package com.escuela.cobros.dto;

import java.time.LocalDateTime;

public record CategoriaMovimientoResponse(
        Long id,
        String codigo,
        String nombre,
        String tipo,
        Boolean esSistema,
        Boolean activo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
