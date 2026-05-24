package com.escuela.cobros.dto;

import java.math.BigDecimal;

/**
 * Resumen financiero-academico de un estudiante para mostrar en la UI al
 * facturar/registrar pago. Combina datos del tipo de curso (precio) con la
 * historia de facturas/pagos.
 */
public record ResumenAcademicoResponse(
    Long estudianteId,
    String nombreCompleto,
    String estadoAcademico,
    String situacionPago,

    /** ID del tipo de curso contratado por el estudiante. */
    Long tipoCursoId,
    String tipoCursoNombre,
    String categoriaLicenciaCodigo,
    Short duracionHoras,

    /** Precio total del curso contratado (= TipoCurso.precioBase). */
    BigDecimal precioCurso,

    /** Suma de monto_original de todas las facturas activas (no anuladas). */
    BigDecimal totalFacturado,

    /** Suma de monto_pagado de todas las facturas activas. */
    BigDecimal totalPagado,

    /** Cuanto debe el estudiante en TOTAL sobre el curso (precioCurso - totalPagado). */
    BigDecimal saldoTotal,

    /** Cuanto debe sobre lo ya facturado (totalFacturado - totalPagado). */
    BigDecimal saldoFacturado,

    /** true si el estudiante tiene un tipo de curso asignado y se pudo calcular. */
    boolean tieneCursoAsignado,

    Integer cantidadFacturas,
    Integer cantidadPagos
) {}
