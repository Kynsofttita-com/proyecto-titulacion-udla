package com.escuela.auth.dto;

import java.util.Map;

public record PlantillaEmailResponse(
        Long id,
        String codigo,
        String asunto,
        String cuerpoHtml,
        Map<String, Object> variables,
        Boolean activa
) {}
