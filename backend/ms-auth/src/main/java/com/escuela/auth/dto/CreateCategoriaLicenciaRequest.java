package com.escuela.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCategoriaLicenciaRequest(
        @NotBlank @Size(max = 20) String codigo,
        @NotBlank String descripcion,
        Boolean activa
) {}
