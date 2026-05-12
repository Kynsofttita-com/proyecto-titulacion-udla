package com.escuela.estudiantes.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

/**
 * Payload de creacion de un Estudiante.
 *
 * <p>Validaciones Ecuador:</p>
 * <ul>
 *   <li>Cedula: 10 digitos numericos (digito verificador validado en service)</li>
 *   <li>Telefono: celular formato 09XXXXXXXX</li>
 *   <li>Genero: M, F, O (opcional)</li>
 * </ul>
 */
public record CreateEstudianteRequest(

        @NotBlank
        @Pattern(regexp = "^\\d{10}$", message = "Cedula debe tener 10 digitos")
        String cedula,

        @NotBlank
        @Size(max = 100)
        String nombre,

        @NotBlank
        @Size(max = 100)
        String apellido,

        @NotBlank
        @Email
        @Size(max = 255)
        String email,

        @NotBlank
        @Pattern(regexp = "^09\\d{8}$", message = "Telefono debe ser celular Ecuador (09XXXXXXXX)")
        String telefono,

        @Size(max = 500)
        String direccion,

        @NotNull
        @Past(message = "Fecha de nacimiento debe ser pasada")
        LocalDate fechaNacimiento,

        @Pattern(regexp = "^[MFO]$", message = "Genero debe ser M, F u O")
        String genero,

        Long tipoCursoId,

        Long categoriaLicenciaId,

        @Size(max = 500)
        String observaciones,

        @Valid
        List<ContactoEmergenciaRequest> contactosEmergencia

) {}
