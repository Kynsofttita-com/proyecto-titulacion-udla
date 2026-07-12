package com.escuela.notificaciones.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;

public record CreatePlantillaRequest(
    @NotBlank(message = "código no puede estar vacío")
    @Size(max = 100, message = "código máximo 100 caracteres")
    String codigo,

    @NotBlank(message = "nombre no puede estar vacío")
    @Size(max = 255, message = "nombre máximo 255 caracteres")
    String nombre,

    @Size(max = 500, message = "descripción máximo 500 caracteres")
    String descripcion,

    @NotBlank(message = "asunto no puede estar vacío")
    @Size(max = 255, message = "asunto máximo 255 caracteres")
    String asunto,

    @NotBlank(message = "contenido no puede estar vacío")
    String contenido,

    @NotNull(message = "activa no puede ser nulo")
    Boolean activa,

    List<Map<String, Object>> variables
) {}
