package com.escuela.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Cambio de contrasenia self-service del usuario autenticado.
 *
 * <p>El gateway agrega {@code X-User-Email} al request; el controller toma
 * la identidad de ahi (no del JSON) para evitar suplantacion.</p>
 */
public record CambiarPasswordRequest(
        @NotBlank
        String currentPassword,

        @NotBlank
        @Size(min = 8, max = 100, message = "La contrasenia debe tener al menos 8 caracteres")
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).+$",
                message = "Password debe incluir mayuscula, minuscula y digito")
        String newPassword
) {}
