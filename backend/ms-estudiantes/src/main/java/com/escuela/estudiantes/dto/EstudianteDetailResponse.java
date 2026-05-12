package com.escuela.estudiantes.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Vista detallada de un Estudiante (GET /estudiantes/{id}).
 * Incluye documentos y contactos de emergencia relacionados.
 */
public record EstudianteDetailResponse(
        Long id,
        String cedula,
        String nombre,
        String apellido,
        String email,
        String telefono,
        String direccion,
        LocalDate fechaNacimiento,
        String genero,
        String estado,
        LocalDate fechaMatricula,
        Long tipoCursoId,
        Long categoriaLicenciaId,
        Long usuarioId,
        String observaciones,
        List<DocumentoResponse> documentos,
        List<ContactoEmergenciaResponse> contactosEmergencia,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
