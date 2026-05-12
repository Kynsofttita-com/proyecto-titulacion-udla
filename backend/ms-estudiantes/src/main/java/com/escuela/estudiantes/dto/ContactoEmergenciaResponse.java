package com.escuela.estudiantes.dto;

public record ContactoEmergenciaResponse(
        Long id,
        String nombre,
        String telefono,
        String parentesco,
        Boolean esPrincipal
) {}
