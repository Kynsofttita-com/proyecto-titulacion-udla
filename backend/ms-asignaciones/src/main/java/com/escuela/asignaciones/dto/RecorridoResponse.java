package com.escuela.asignaciones.dto;

import java.time.LocalDateTime;

/**
 * Resumen del recorrido de una clase ya iniciada/finalizada.
 */
public record RecorridoResponse(
        Long asignacionId,
        Long vehiculoId,
        String estado,
        Integer kmInicial,
        Integer kmFinal,
        Integer kmRecorridos,         // kmFinal - kmInicial (null si no esta finalizada)
        LocalDateTime horaInicioReal,
        LocalDateTime horaFinReal,
        Long duracionRealMinutos,     // diferencia en minutos (null si no finalizada)
        String observacionesRecorrido,
        Boolean syncVehiculoExitoso,  // true si ms-vehiculos confirmo el update
        String mensajeSyncVehiculo,
        Boolean syncEstudianteExitoso, // true si ms-estudiantes sumó las horas
        String mensajeSyncEstudiante
) {}
