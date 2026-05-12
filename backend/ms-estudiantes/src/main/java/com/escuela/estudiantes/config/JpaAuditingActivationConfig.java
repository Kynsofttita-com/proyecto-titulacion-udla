package com.escuela.estudiantes.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Activa JPA Auditing en este microservicio.
 *
 * <p>Separado de la clase Application principal para que pruebas tipo
 * {@code @WebMvcTest} (que solo cargan la capa web) no fallen al intentar
 * resolver beans JPA inexistentes en su contexto.</p>
 *
 * <p>El bean {@code auditorAware} se registra automaticamente desde
 * {@code common-jpa} (autoconfig).</p>
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditingActivationConfig {
}
