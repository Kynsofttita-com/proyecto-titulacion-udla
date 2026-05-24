package com.escuela.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "La contrasenia actual es requerida")
        String currentPassword,

        @NotBlank(message = "La nueva contrasenia es requerida")
        @Size(min = 8, message = "La contrasenia debe tener al menos 8 caracteres")
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).+$",
                message = "La contrasenia debe incluir mayuscula, minuscula y digito")
        String newPassword
) {}
