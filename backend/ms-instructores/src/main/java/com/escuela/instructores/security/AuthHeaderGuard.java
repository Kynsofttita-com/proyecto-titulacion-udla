package com.escuela.instructores.security;

import com.escuela.instructores.exception.NoAutenticadoException;
import com.escuela.instructores.exception.SinPermisoException;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public final class AuthHeaderGuard {

    private AuthHeaderGuard() { }

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
