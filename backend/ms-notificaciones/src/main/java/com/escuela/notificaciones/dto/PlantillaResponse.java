package com.escuela.notificaciones.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record PlantillaResponse(
    Long id,
    String codigo,
    String nombre,
    String descripcion,
    String asunto,
    String contenido,
    Boolean activa,
    List<Map<String, Object>> variables,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    String createdBy,
    String updatedBy
) {}
