package com.escuela.common.security.headers;

import java.util.List;

/**
 * Representa al usuario autenticado en el contexto del request actual.
 *
 * <p>Construido por el API Gateway (T4.3) tras validar el JWT y enviado
 * en headers {@code X-User-*} a los microservicios downstream. Cada MS
 * usa {@link UserContextHolder} para acceder a esta info de forma
 * thread-safe sin tener que leer headers manualmente.</p>
 *
 * <p>Inmutable.</p>
 */
public record UserContext(
        Long userId,
        String email,
        List<String> roles
) {

    /** Indica si el usuario tiene un rol especifico (case-sensitive). */
    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }

    /** Indica si el usuario tiene cualquiera de los roles indicados. */
    public boolean hasAnyRole(String... checkRoles) {
        if (roles == null) return false;
        for (String r : checkRoles) {
            if (roles.contains(r)) return true;
        }
        return false;
    }
}
