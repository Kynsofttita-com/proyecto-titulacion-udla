package com.escuela.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ResetPasswordRequest(
        @NotNull
        UUID token,

        @NotBlank @Size(min = 8, max = 100, message = "La contrasenia debe tener al menos 8 caracteres")
        String newPassword
) {}
