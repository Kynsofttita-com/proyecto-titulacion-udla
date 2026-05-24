package com.escuela.cobros.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/**
 * Vista parcial de TipoCurso vista desde MS-Cobros. Solo nos importa precio y
 * datos descriptivos para presentar en la UI.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TipoCursoDTO(
    Long id,
    String nombre,
    String descripcion,
    Short duracionTotalHoras,
    BigDecimal precioBase,
    Long categoriaLicenciaId,
    String categoriaLicenciaCodigo
) {}
