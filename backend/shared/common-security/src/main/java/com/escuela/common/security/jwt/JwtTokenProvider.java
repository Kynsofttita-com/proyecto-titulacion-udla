package com.escuela.common.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Generador y validador de tokens JWT firmados con HS512.
 *
 * <p>Genera tokens de tipo {@link TokenType#ACCESS} y {@link TokenType#REFRESH}
 * con metadatos: subject (userId), email, roles, tipo, jti (UUID unico), iat, exp.</p>
 *
 * <p>Esta clase es <b>stateless</b>: no persiste tokens. La rotation y la
 * revocacion del refresh token se manejan en MS-Auth (T4.2) con la tabla
 * {@code auth_schema.refresh_tokens} (que se creara entonces).</p>
 *
 * <p>Uso desde MS-Auth:</p>
 * <pre>
 * String access = provider.generateAccessToken(userId, email, roles);
 * String refresh = provider.generateRefreshToken(userId, email, roles);
 * </pre>
 *
 * <p>Uso desde API Gateway (validacion):</p>
 * <pre>
 * try {
 *     ParsedJwt parsed = provider.parse(token);
 *     // parsed.userId(), parsed.email(), parsed.roles()
 * } catch (JwtException e) {
 *     // Token invalido o expirado -> 401
 * }
 * </pre>
 */
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final JwtProperties properties;
    private final SecretKey signingKey;

    public JwtTokenProvider(JwtProperties properties) {
        this.properties = properties;
        this.signingKey = buildKey(properties.getSecret());
    }

    private static SecretKey buildKey(String secret) {
        if (secret == null || secret.length() < 64) {
            throw new IllegalArgumentException(
                    "JWT secret debe tener al menos 64 caracteres (512 bits) para HS512. " +
                    "Generar con: openssl rand -hex 64");
        }
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // -----------------------------------------------------------------------
    // Generacion
    // -----------------------------------------------------------------------

    public String generateAccessToken(Long userId, String email, List<String> roles) {
        return generateToken(TokenType.ACCESS, userId, email, roles,
                ChronoUnit.MINUTES, properties.getAccessTokenExpirationMinutes());
    }

    public String generateRefreshToken(Long userId, String email, List<String> roles) {
        return generateToken(TokenType.REFRESH, userId, email, roles,
                ChronoUnit.DAYS, properties.getRefreshTokenExpirationDays());
    }

    private String generateToken(TokenType type, Long userId, String email,
                                 List<String> roles, ChronoUnit unit, long amount) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(amount, unit);
        UUID jti = UUID.randomUUID();

        return Jwts.builder()
                .id(jti.toString())
                .subject(String.valueOf(userId))
                .issuer(properties.getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .claim(JwtClaims.EMAIL, email)
                .claim(JwtClaims.ROLES, roles)
                .claim(JwtClaims.TOKEN_TYPE, type.name())
                .signWith(signingKey, Jwts.SIG.HS512)
                .compact();
    }

    // -----------------------------------------------------------------------
    // Validacion / Parse
    // -----------------------------------------------------------------------

    /**
     * Parsea y valida un JWT. Lanza {@link JwtException} si es invalido,
     * expirado, malformado o tiene firma incorrecta.
     */
    public ParsedJwt parse(String token) {
        try {
            Jws<Claims> jws = Jwts.parser()
                    .verifyWith(signingKey)
                    .requireIssuer(properties.getIssuer())
                    .build()
                    .parseSignedClaims(token);

            Claims c = jws.getPayload();

            Long userId = Long.valueOf(c.getSubject());
            String email = c.get(JwtClaims.EMAIL, String.class);
            @SuppressWarnings("unchecked")
            List<String> roles = c.get(JwtClaims.ROLES, List.class);
            TokenType type = TokenType.valueOf(c.get(JwtClaims.TOKEN_TYPE, String.class));
            UUID jti = UUID.fromString(c.getId());
            Instant iat = c.getIssuedAt().toInstant();
            Instant exp = c.getExpiration().toInstant();

            return new ParsedJwt(userId, email, roles, type, jti, iat, exp);

        } catch (ExpiredJwtException e) {
            log.debug("Token expirado: {}", e.getMessage());
            throw e;
        } catch (SignatureException e) {
            log.warn("Firma JWT invalida: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            log.warn("JWT malformado: {}", e.getMessage());
            throw e;
        } catch (JwtException e) {
            log.warn("Error parseando JWT: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Variante "soft" de {@link #parse(String)}: devuelve {@code true} si el
     * token es valido sin lanzar excepcion. Util para checks rapidos.
     */
    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
