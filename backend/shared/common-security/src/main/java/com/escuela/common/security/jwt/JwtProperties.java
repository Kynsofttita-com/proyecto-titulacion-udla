package com.escuela.common.security.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propiedades de configuracion del subsistema JWT.
 *
 * <p>Mapeadas desde el namespace {@code escuela.security.jwt} en
 * {@code application.yml}. Ejemplo:</p>
 *
 * <pre>
 * escuela:
 *   security:
 *     jwt:
 *       secret: ${JWT_SECRET}
 *       access-token-expiration-minutes: 15
 *       refresh-token-expiration-days: 7
 *       issuer: escuela-conduccion
 * </pre>
 *
 * <p>El {@code secret} debe ser una cadena de al menos 64 caracteres (512 bits)
 * para HS512. Generar con: {@code openssl rand -hex 64}.</p>
 */
@Data
@ConfigurationProperties(prefix = "escuela.security.jwt")
public class JwtProperties {

    /** Clave secreta para firmar tokens HS512. Minimo 64 caracteres (512 bits). */
    private String secret;

    /** Duracion del access token en minutos. Default 15. */
    private int accessTokenExpirationMinutes = 15;

    /** Duracion del refresh token en dias. Default 7. */
    private int refreshTokenExpirationDays = 7;

    /** Issuer del JWT (claim "iss"). Default "escuela-conduccion". */
    private String issuer = "escuela-conduccion";
}
