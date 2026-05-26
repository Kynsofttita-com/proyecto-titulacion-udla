package com.escuela.auth.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record UpdateUsuarioRequest(
        @Size(max = 100) String nombre,
        @Size(max = 100) String apellido,
        @Pattern(regexp = "^0\\d{9}$|^$", message = "Telefono ecuatoriano invalido")
        String telefono,
        Boolean activo,
        @Size(max = 10) String cedula,
        LocalDate fechaNacimiento,
        String genero,
        String direccion,
        String ciudad,
        String provincia,
        List<String> roles,
        String email,
        Boolean passwordChangeRequired
) {}
