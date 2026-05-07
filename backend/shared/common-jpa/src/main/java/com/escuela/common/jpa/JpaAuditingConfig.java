package com.escuela.common.jpa;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;

/**
 * Auto-configuración compartida para JPA Auditing.
 *
 * <p>Se carga automáticamente en todos los microservicios que tengan
 * {@code common-jpa} en classpath, gracias al archivo
 * {@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports}.</p>
 *
 * <p>Registra el bean {@link AuditorAware} con la implementación
 * {@link AuditorAwareImpl} solo si no hay otro bean ya registrado.</p>
 *
 * <p>Cada microservicio que use BaseEntity necesita además agregar
 * {@code @EnableJpaAuditing(auditorAwareRef = "auditorAware")} en su clase
 * {@code Application} para activar el listener de auditing.</p>
 */
@AutoConfiguration
@ConditionalOnClass(AuditorAware.class)
public class JpaAuditingConfig {

    /**
     * Provee el {@link AuditorAware} usado para popular {@code createdBy}
     * y {@code updatedBy}. Lee el usuario actual desde Spring SecurityContext
     * o devuelve "system" si no hay autenticación.
     */
    @Bean
    @ConditionalOnMissingBean(AuditorAware.class)
    public AuditorAware<String> auditorAware() {
        return new AuditorAwareImpl();
    }
}
