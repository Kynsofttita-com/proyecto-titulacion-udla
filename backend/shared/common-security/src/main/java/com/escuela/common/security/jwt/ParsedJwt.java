package com.escuela.common.security.jwt;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Resultado del parsing exitoso de un JWT.
 *
 * <p>Inmutable. Si el parsing falla (firma invalida, expirado, malformado),
 * {@link JwtTokenProvider#parse(String)} lanza una excepcion en vez de
 * devolver este record.</p>
 */
public record ParsedJwt(
        Long userId,
        String email,
        List<String> roles,
        TokenType tokenType,
        UUID jti,
        Instant issuedAt,
        Instant expiresAt
) {}
