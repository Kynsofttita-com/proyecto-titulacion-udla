package com.escuela.estudiantes.security;

import com.escuela.estudiantes.controller.EstudianteController.NoAutenticadoException;
import com.escuela.estudiantes.controller.EstudianteController.SinPermisoException;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Utilidad para validar autenticacion y rol en sub-recursos de Estudiantes.
 *
 * <p>El API Gateway valida el JWT y propaga {@code X-User-Email} y
 * {@code X-User-Roles}. MS-Estudiantes confia en esos headers (no tiene
 * filtro JWT propio).</p>
 */
public final class AuthHeaderGuard {

    private AuthHeaderGuard() {
        // utility class
    }

    public static void requireAuth(String userEmail) {
        if (userEmail == null || userEmail.isBlank()) {
            throw new NoAutenticadoException();
        }
    }

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
