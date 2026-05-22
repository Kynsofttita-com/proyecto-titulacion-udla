package com.escuela.vehiculos.security;

import com.escuela.vehiculos.controller.VehiculoController;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public final class AuthHeaderGuard {

    private AuthHeaderGuard() { }

    public static void requireAuth(String userEmail) {
        if (userEmail == null || userEmail.isBlank()) {
            throw new VehiculoController.NoAutenticadoException();
        }
    }

    public static void requireAnyRole(String userRolesHeader, Set<String> rolesPermitidos) {
        if (userRolesHeader == null || userRolesHeader.isBlank()) {
            throw new VehiculoController.SinPermisoException();
        }
        List<String> roles = Arrays.asList(userRolesHeader.split("\\s*,\\s*"));
        if (roles.stream().noneMatch(rolesPermitidos::contains)) {
            throw new VehiculoController.SinPermisoException();
        }
    }
}
