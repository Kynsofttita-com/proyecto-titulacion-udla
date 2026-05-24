package com.escuela.estudiantes.dto;

import java.time.LocalDate;

/**
 * Vista compacta para listados paginados (GET /estudiantes).
 * Concatena nombre + apellido en un solo campo para reducir payload.
 */
public record EstudianteListResponse(
        Long id,
        String cedula,
        String nombreCompleto,
        String email,
        String telefono,
        String estado,
        String situacionPago,
        LocalDate fechaMatricula
) {}
