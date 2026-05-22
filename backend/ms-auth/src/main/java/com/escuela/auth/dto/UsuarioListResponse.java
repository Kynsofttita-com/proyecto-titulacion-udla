package com.escuela.auth.dto;

import java.time.LocalDateTime;
import java.util.List;

/** Resumen para listados (sin datos sensibles). */
public record UsuarioListResponse(
        Long id,
        String email,
        String nombre,
        String apellido,
        Boolean activo,
        Boolean locked,
        LocalDateTime lastLogin,
        List<String> roles
) {}
