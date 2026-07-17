package com.escuela.instructores.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificacionResponse {
    private Long id;
    private String tipo;
    private LocalDate fechaObtencion;
    private LocalDate vigenciaHasta;
    private String entidadEmisora;
    private String archivoUrl;
    private String observaciones;
}
