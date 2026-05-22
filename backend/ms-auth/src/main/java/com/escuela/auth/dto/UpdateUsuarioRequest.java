package com.escuela.auth.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUsuarioRequest(
        @Size(max = 100) String nombre,
        @Size(max = 100) String apellido,
        @Pattern(regexp = "^0\\d{9}$|^$", message = "Telefono ecuatoriano invalido")
        String telefono,
        Boolean activo
) {}
