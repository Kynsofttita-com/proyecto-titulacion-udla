package com.escuela.notificaciones.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propiedades de notificaciones (mapea {@code escuela.notificaciones.*}).
 */
@Data
@ConfigurationProperties(prefix = "escuela.notificaciones")
public class NotificacionesProperties {

    /**
     * Si false, los listeners persisten en log_envios_email con estado
     * PENDIENTE pero NO envian email real. Util para dev sin SMTP credentials.
     */
    private boolean mailEnabled = false;

    private String fromEmail = "no-reply@escuela-conduccion.local";
    private String fromName = "Escuela de Conduccion";

    /** URL base del frontend (para construir links en emails). */
    private String frontendBaseUrl = "http://localhost:5173";
}
