package com.escuela.auth.dto;

import java.util.List;

/**
 * Respuesta del login exitoso. Los tokens tambien se setean como cookies
 * HttpOnly por el controller (el JSON los expone para clientes que no usan
 * cookies, ej. Postman/tests).
 */
public record LoginResponse(
        String accessToken,
        String refreshToken,
        long accessTokenExpiresInSeconds,
        long refreshTokenExpiresInSeconds,
        UserInfo user
) {

    public record UserInfo(
            Long id,
            String email,
            String nombre,
            String apellido,
            List<String> roles,
            Boolean mustChangePassword
    ) {}
}
