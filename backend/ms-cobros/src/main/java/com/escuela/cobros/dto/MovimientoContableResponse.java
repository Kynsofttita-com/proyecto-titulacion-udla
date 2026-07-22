package com.escuela.cobros.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record MovimientoContableResponse(
        Long id,
        LocalDate fecha,
        String tipo,
        BigDecimal monto,
        Long cuentaId,
        String cuentaNombre,
        Long categoriaId,
        String categoriaCodigo,
        String categoriaNombre,
        String descripcion,
        String referencia,
        Long pagoId,
        // Origen "Vehiculo" (opcional): NULL en movimientos manuales o de pago
        Long registroCombustibleId,
        Long mantenimientoId,
        Long vehiculoId,
        String placaVehiculo,
        Integer kilometraje,
        // Persona a la que se le paga (opcional, categorias de sueldo)
        Long pagadoAId,
        String nombrePagadoA,
        Boolean anulado,
        String motivoAnulacion,
        LocalDateTime createdAt,
        String createdBy
) {}
