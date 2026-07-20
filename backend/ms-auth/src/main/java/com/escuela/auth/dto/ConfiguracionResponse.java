package com.escuela.auth.dto;

import java.time.LocalTime;

public record ConfiguracionResponse(
        Long id,
        String nombre,
        String ruc,
        String direccion,
        String telefono,
        String email,
        String logoUrl,
        String colorPrimario,
        String colorSecundario,
        Short duracionClaseDefaultMin,
        LocalTime horarioApertura,
        LocalTime horarioCierre,
        Short horasRecordatorioClase,
        Short diasAlertaSoat,
        Short maxIntentosFallidos,
        Short duracionBloqueoMinutos,
        Short expiracionTokenResetMinutos
) {}
