package com.escuela.notificaciones.dto;

import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;

public record UpdatePlantillaRequest(
    @Size(max = 255, message = "nombre máximo 255 caracteres")
    String nombre,

    @Size(max = 500, message = "descripción máximo 500 caracteres")
    String descripcion,

    @Size(max = 255, message = "asunto máximo 255 caracteres")
    String asunto,

    String contenido,

    Boolean activa,

    List<Map<String, Object>> variables
) {}
