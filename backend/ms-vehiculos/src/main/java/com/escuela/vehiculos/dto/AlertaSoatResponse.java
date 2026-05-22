package com.escuela.vehiculos.dto;

import java.time.LocalDate;

/** Resumen de alerta para vehiculos con SOAT por vencer en N dias o ya vencido. */
public record AlertaSoatResponse(
        Long vehiculoId,
        String placa,
        String marca,
        String modelo,
        LocalDate soatVencimiento,
        long diasParaVencer,   // negativo si ya vencio
        Boolean vencido
) {}
