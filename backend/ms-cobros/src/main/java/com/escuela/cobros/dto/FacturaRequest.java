package com.escuela.cobros.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request para crear o actualizar una factura.
 *
 * <p>Soporta dos modalidades:
 * <ul>
 *   <li><b>CONTADO</b> (default): {@code tipoPago=CONTADO}, {@code numeroCuotas=1}.
 *       Los campos {@code frecuenciaCuota} y {@code fechaPrimeraCuota} pueden ser null.</li>
 *   <li><b>CREDITO</b>: {@code tipoPago=CREDITO}, {@code numeroCuotas} entre 2 y 24,
 *       {@code frecuenciaCuota} y {@code fechaPrimeraCuota} obligatorios. El servicio
 *       generará automáticamente las filas en {@code factura_cuotas}.</li>
 * </ul>
 */
public record FacturaRequest(
    @NotNull(message = "El ID del estudiante no puede ser nulo")
    Long estudianteId,

    @NotNull(message = "El ID del concepto de facturación no puede ser nulo")
    Long conceptoFacturacionId,

    @NotNull(message = "El monto original no puede ser nulo")
    @DecimalMin(value = "0.01", message = "El monto original debe ser mayor a 0")
    BigDecimal montoOriginal,

    @NotNull(message = "La fecha de vencimiento no puede ser nula")
    @FutureOrPresent(message = "La fecha de vencimiento debe ser presente o futura")
    LocalDate fechaVencimiento,

    String descripcion,

    // ============== Campos opcionales de crédito ==============

    @Pattern(regexp = "CONTADO|CREDITO", message = "tipoPago debe ser CONTADO o CREDITO")
    String tipoPago,

    @Min(value = 1, message = "numeroCuotas debe ser >= 1")
    @Max(value = 24, message = "numeroCuotas debe ser <= 24")
    Integer numeroCuotas,

    @Pattern(regexp = "MENSUAL|QUINCENAL|SEMANAL", message = "frecuenciaCuota debe ser MENSUAL, QUINCENAL o SEMANAL")
    String frecuenciaCuota,

    LocalDate fechaPrimeraCuota
) {
    public FacturaRequest {
        if (tipoPago == null) tipoPago = "CONTADO";
        if (numeroCuotas == null) numeroCuotas = 1;
    }
}
