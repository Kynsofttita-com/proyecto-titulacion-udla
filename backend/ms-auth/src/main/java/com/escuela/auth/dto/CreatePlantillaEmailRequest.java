package com.escuela.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record CreatePlantillaEmailRequest(
        @NotBlank @Size(max = 100) String codigo,
        @NotBlank String asunto,
        @NotBlank String cuerpoHtml,
        Map<String, Object> variables,
        Boolean activa
) {}
