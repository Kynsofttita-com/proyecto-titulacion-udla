package com.escuela.auth.dto;

import jakarta.validation.constraints.Size;

import java.util.Map;

public record UpdatePlantillaEmailRequest(
        @Size(max = 100) String codigo,
        String asunto,
        String cuerpoHtml,
        Map<String, Object> variables,
        Boolean activa
) {}
