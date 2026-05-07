package com.escuela.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propiedades configurables del subsistema de autenticacion (lockout +
 * forgot-password). Mapeadas desde {@code escuela.security.auth.*}.
 */
@Data
@ConfigurationProperties(prefix = "escuela.security.auth")
public class AuthProperties {

    /** Numero de intentos fallidos antes de bloquear la cuenta. Default 3. */
    private int maxFailedAttempts = 3;

    /** Duracion del lockout en minutos. Default 15. */
    private int lockoutDurationMinutes = 15;

    /** Duracion del token de reset password en minutos. Default 60. */
    private int resetTokenExpirationMinutes = 60;
}
