package com.escuela.instructores.dto;

import java.time.LocalDate;

public record InstructorListResponse(
        Long id,
        String cedula,
        String nombre,
        String apellido,
        String email,
        String licenciaCategoria,
        LocalDate licenciaCaducidad,
        String estado
) {}
