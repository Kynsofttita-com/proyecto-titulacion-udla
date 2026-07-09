package com.escuela.notificaciones.dto;

import java.time.LocalDateTime;

public record PreferenciaResponse(
    Long id,
    Long usuarioId,
    Boolean recibirEmail,
    Boolean recibirInApp,
    Boolean recibirRecordatorios,
    Boolean recibirAlertasAdmin,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    String createdBy,
    String updatedBy
) {}
