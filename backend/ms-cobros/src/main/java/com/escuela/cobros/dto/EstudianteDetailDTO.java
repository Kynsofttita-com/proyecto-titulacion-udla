package com.escuela.cobros.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Vista parcial del estudiante usada por MS-Cobros via Feign a MS-Estudiantes.
 * Jackson ignora campos extra que vengan en la respuesta (ej: documentos,
 * contactos), solo deserializa los listados aqui.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EstudianteDetailDTO(
    Long id,
    String estado,
    String situacionPago,
    String nombre,
    String apellido,
    String cedula,
    Long tipoCursoId,
    Long categoriaLicenciaId,
    String email,
    Long usuarioId
) {
    /** Constructor de compatibilidad usado por el fallback del FeignClient. */
    public EstudianteDetailDTO(Long id, String estado) {
        this(id, estado, null, null, null, null, null, null, null, null);
    }

    public String nombreCompleto() {
        return (nombre != null ? nombre : "") + " " + (apellido != null ? apellido : "");
    }
}
