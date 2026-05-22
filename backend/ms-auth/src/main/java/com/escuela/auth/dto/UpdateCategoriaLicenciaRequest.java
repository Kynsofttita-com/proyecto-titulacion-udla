package com.escuela.auth.dto;

import jakarta.validation.constraints.Size;

public record UpdateCategoriaLicenciaRequest(
        @Size(max = 20) String codigo,
        String descripcion,
        Boolean activa
) {}
