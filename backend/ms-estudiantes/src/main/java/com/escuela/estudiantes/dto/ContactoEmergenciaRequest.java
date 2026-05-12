package com.escuela.estudiantes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ContactoEmergenciaRequest(

        @NotBlank
        @Size(max = 200)
        String nombre,

        @NotBlank
        @Pattern(regexp = "^09\\d{8}$", message = "Telefono debe ser celular Ecuador (09XXXXXXXX)")
        String telefono,

        @Size(max = 50)
        String parentesco,

        Boolean esPrincipal

) {}
