package com.escuela.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreatePlantillaEmailRequest(
        @NotBlank @Size(max = 100) String codigo,
        @NotBlank String asunto,
        @NotBlank String cuerpoHtml,
        List<String> variables,
        Boolean activa
) {}
