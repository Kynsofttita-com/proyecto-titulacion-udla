package com.escuela.estudiantes.dto;

import java.time.LocalDate;

/**
 * Vista compacta para listados paginados (GET /estudiantes).
 * Incluye avance académico (horas) para mostrar en tabla de lista.
 */
public record EstudianteListResponse(
        Long id,
        String cedula,
        String nombreCompleto,
        String email,
        String telefono,
        String estado,
        String situacionPago,
        LocalDate fechaMatricula,
        Integer horasCompletadas,
        Integer horasRequeridas
) {}
