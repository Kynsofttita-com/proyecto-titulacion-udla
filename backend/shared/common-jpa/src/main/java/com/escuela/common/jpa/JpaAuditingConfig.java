package com.escuela.common.jpa;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Habilita JPA Auditing para todos los microservicios que importen
 * {@code common-jpa}.
 *
 * <p>Registra automáticamente el bean {@link AuditorAware} con la implementación
 * {@link AuditorAwareImpl} si no hay otro definido en el contexto.</p>
 *
 * <p>Cada microservicio que use BaseEntity solo necesita importar
 * {@code common-jpa} y agregar {@code @EnableJpaAuditing} en su clase Application
 * (apuntando a {@code auditorAwareRef = "auditorAware"}).</p>
 *
 * <p>Ya que cada microservicio tiene su propia {@code @SpringBootApplication}
 * con {@code @EnableJpaAuditing}, este config solo expone el bean {@link AuditorAware}
 * cuando es solicitado.</p>
 */
@Configuration
public class JpaAuditingConfig {

    @Bean
    @ConditionalOnMissingBean(AuditorAware.class)
    public AuditorAware<String> auditorAware() {
        return new AuditorAwareImpl();
    }
}
