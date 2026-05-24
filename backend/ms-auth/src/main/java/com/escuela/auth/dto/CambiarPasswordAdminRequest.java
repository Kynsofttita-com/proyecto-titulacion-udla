package com.escuela.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CambiarPasswordAdminRequest(
        @NotBlank
        @Size(min = 6, message = "Password debe tener al menos 6 caracteres")
        String nuevaPassword,
        Boolean passwordChangeRequired
) {}
