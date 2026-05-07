package com.escuela.common.jpa;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Implementación de {@link AuditorAware} que provee el identificador del usuario
 * actual desde Spring SecurityContext.
 *
 * <p>Usado por {@link org.springframework.data.jpa.domain.support.AuditingEntityListener}
 * para popular automáticamente los campos {@code createdBy} y {@code updatedBy}
 * de las entidades que extienden {@link BaseEntity}.</p>
 *
 * <p>Comportamiento:</p>
 * <ul>
 *   <li>Si hay un usuario autenticado: devuelve su nombre/email</li>
 *   <li>Si no hay autenticación o es anónima: devuelve {@code "system"}</li>
 *   <li>Truncado a 50 caracteres (longitud del campo en BD)</li>
 * </ul>
 *
 * <p>Para activar el auditing global, registrar este bean en una clase
 * {@code @Configuration} con {@code @EnableJpaAuditing(auditorAwareRef = "auditorAware")}
 * o ver {@link JpaAuditingConfig}.</p>
 */
public class AuditorAwareImpl implements AuditorAware<String> {

    private static final String SYSTEM_USER = "system";
    private static final int MAX_LENGTH = 50;

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.of(SYSTEM_USER);
        }

        String name = authentication.getName();
        if (name == null || name.isBlank()) {
            return Optional.of(SYSTEM_USER);
        }

        return Optional.of(name.length() > MAX_LENGTH ? name.substring(0, MAX_LENGTH) : name);
    }
}
