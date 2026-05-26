package com.escuela.auth.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Detalle de un usuario (sin password). */
public record UsuarioResponse(
        Long id,
        String email,
        String nombre,
        String apellido,
        String telefono,
        Boolean activo,
        Boolean locked,
        Short failedAttempts,
        LocalDateTime lockUntil,
        LocalDateTime lastLogin,
        List<String> roles,
        String cedula,
        LocalDate fechaNacimiento,
        String genero,
        String direccion,
        String ciudad,
        String provincia,
        Boolean passwordChangeRequired
) {}
