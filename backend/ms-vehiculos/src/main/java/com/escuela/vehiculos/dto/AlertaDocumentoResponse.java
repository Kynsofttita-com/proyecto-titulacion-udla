package com.escuela.vehiculos.dto;

import java.time.LocalDate;

/**
 * Alerta unificada de vencimiento de documentos del vehiculo (SOAT o RTV).
 * Reemplaza al {@link AlertaSoatResponse} cuando se necesita mostrar ambos
 * tipos en el mismo widget/panel.
 */
public record AlertaDocumentoResponse(
        Long vehiculoId,
        String placa,
        String marca,
        String modelo,
        /** SOAT o RTV. */
        String tipoDocumento,
        LocalDate fechaVencimiento,
        /** Negativo si ya vencio. */
        long diasParaVencer,
        boolean vencido
) {}
