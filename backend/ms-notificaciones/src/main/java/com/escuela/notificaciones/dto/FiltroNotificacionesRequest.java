package com.escuela.notificaciones.dto;

public record FiltroNotificacionesRequest(
    Boolean leida,
    String tipo,
    String prioridad
) {}
