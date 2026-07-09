package com.escuela.notificaciones.dto;

public record UpdatePreferenciaRequest(
    Boolean recibirEmail,
    Boolean recibirInApp,
    Boolean recibirRecordatorios,
    Boolean recibirAlertasAdmin
) {}
