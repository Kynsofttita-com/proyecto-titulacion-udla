package com.escuela.auth.dto;

import java.util.List;

public record PlantillaEmailResponse(
        Long id,
        String codigo,
        String asunto,
        String cuerpoHtml,
        List<String> variables,
        Boolean activa
) {}
