package com.escuela.estudiantes.dto;

public record UpdateAsistenciaRequest(
        Boolean asistio,
        String justificacion,
        String observaciones
) {}
