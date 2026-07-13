package com.escuela.instructores.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

public record InstructorListResponse(
        Long id,
        String cedula,
        String nombre,
        String apellido,
        String email,
        String telefono,
        String licenciaNumero,
        String licenciaCategoria,
        LocalDate licenciaCaducidad,
        String estado,
        String tipoContrato
) {
    @JsonProperty("nombreCompleto")
    public String getNombreCompleto() {
        if (nombre == null) return "";
        if (apellido == null) return nombre;
        return nombre + " " + apellido;
    }
}
