package com.escuela.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record CreateUsuarioRequest(
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank
        @Size(min = 8, message = "Password debe tener al menos 8 caracteres")
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).+$",
                message = "Password debe incluir mayuscula, minuscula y digito")
        String password,
        @NotBlank @Size(max = 100) String nombre,
        @NotBlank @Size(max = 100) String apellido,
        @Pattern(regexp = "^0\\d{9}$|^$", message = "Telefono ecuatoriano invalido")
        String telefono,
        @NotEmpty(message = "Debe asignarse al menos un rol")
        List<String> roles,
        @Size(max = 10) String cedula,
        LocalDate fechaNacimiento,
        String genero,
        String direccion,
        String ciudad,
        String provincia,
        Boolean passwordChangeRequired
) {}
