package com.escuela.estudiantes.dto;

import java.time.LocalDate;

/**
 * Vista publica resumida de un Estudiante. Usada en POST/PUT response.
 */
public record EstudianteResponse(
        Long id,
        String cedula,
        String nombre,
        String apellido,
        String email,
        String telefono,
        String estado,
        LocalDate fechaMatricula
) {}
