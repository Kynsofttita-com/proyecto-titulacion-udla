package com.escuela.notificaciones.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record NotificacionResponse(
    Long id,
    Long usuarioId,
    String tipo,
    String titulo,
    String mensaje,
    Boolean leida,
    LocalDateTime fechaCreacion,
    LocalDateTime fechaLectura,
    Map<String, Object> datosExtra,
    String prioridad,
    LocalDateTime createdAt,
    String createdBy,
    LocalDateTime deletedAt
) {}
