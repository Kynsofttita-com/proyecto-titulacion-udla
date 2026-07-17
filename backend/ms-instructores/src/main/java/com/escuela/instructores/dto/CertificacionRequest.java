package com.escuela.instructores.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificacionRequest {
    @NotBlank @Size(max = 100)
    private String tipo;

    @NotNull
    private LocalDate fechaObtencion;

    private LocalDate vigenciaHasta;
    private String entidadEmisora;

    @Size(max = 500)
    private String archivoUrl;

    private String observaciones;
}
