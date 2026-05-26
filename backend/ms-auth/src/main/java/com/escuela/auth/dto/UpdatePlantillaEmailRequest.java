package com.escuela.auth.dto;

import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdatePlantillaEmailRequest(
        @Size(max = 100) String codigo,
        String asunto,
        String cuerpoHtml,
        List<String> variables,
        Boolean activa
) {}
