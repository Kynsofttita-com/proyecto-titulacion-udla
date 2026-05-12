package com.escuela.estudiantes.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Payload de actualizacion de un Estudiante.
 *
 * <p>NO permite cambiar la cedula. Todos los campos son opcionales (PATCH-like
 * semantics): solo los enviados se actualizan. La validacion se aplica solo si
 * el campo viene presente (no @NotBlank/@NotNull).</p>
 */
public record UpdateEstudianteRequest(

        @Size(max = 100)
        String nombre,

        @Size(max = 100)
        String apellido,

        @Email
        @Size(max = 255)
        String email,

        @Pattern(regexp = "^09\\d{8}$", message = "Telefono debe ser celular Ecuador (09XXXXXXXX)")
        String telefono,

        @Size(max = 500)
        String direccion,

        @Past(message = "Fecha de nacimiento debe ser pasada")
        LocalDate fechaNacimiento,

        @Pattern(regexp = "^[MFO]$", message = "Genero debe ser M, F u O")
        String genero,

        @Pattern(regexp = "^(PRE_MATRICULADO|ACTIVO|COMPLETADO|RETIRADO)$",
                message = "Estado invalido")
        String estado,

        Long tipoCursoId,

        Long categoriaLicenciaId,

        @Size(max = 500)
        String observaciones

) {}
