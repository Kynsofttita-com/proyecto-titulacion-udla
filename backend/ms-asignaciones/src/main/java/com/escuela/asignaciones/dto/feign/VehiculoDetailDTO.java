package com.escuela.asignaciones.dto.feign;

import java.time.LocalDateTime;

public record VehiculoDetailDTO(
        Long id,
        LocalDateTime deletedAt
) {}
