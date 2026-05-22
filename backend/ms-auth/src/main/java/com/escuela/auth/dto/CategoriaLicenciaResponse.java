package com.escuela.auth.dto;

public record CategoriaLicenciaResponse(
        Long id,
        String codigo,
        String descripcion,
        Boolean activa
) {}
