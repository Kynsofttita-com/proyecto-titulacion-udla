package com.escuela.auth.security;

import com.escuela.auth.exception.NoAutenticadoException;
import com.escuela.auth.exception.SinPermisoException;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Utilidad para validar autenticacion y rol en los controllers de MS-Auth.
 *
 * <p>El API Gateway valida el JWT y propaga {@code X-User-Email} y
 * {@code X-User-Roles} (separados por coma). MS-Auth confia en esos headers.</p>
 */
public final class AuthHeaderGuard {

    private AuthHeaderGuard() {
        // utility class
    }

    /** Lanza {@link NoAutenticadoException} (401) si el email es null o blanco. */
    public static void requireAuth(String userEmail) {
        if (userEmail == null || userEmail.isBlank()) {
            throw new NoAutenticadoException();
        }
    }

    /**
     * Lanza {@link SinPermisoException} (403) si el usuario NO tiene al menos
     * uno de los roles permitidos.
     */
    public static void requireAnyRole(String userRolesHeader, Set<String> rolesPermitidos) {
        if (userRolesHeader == null || userRolesHeader.isBlank()) {
            throw new SinPermisoException();
        }
        List<String> roles = Arrays.asList(userRolesHeader.split("\\s*,\\s*"));
        if (roles.stream().noneMatch(rolesPermitidos::contains)) {
            throw new SinPermisoException();
        }
    }
}
