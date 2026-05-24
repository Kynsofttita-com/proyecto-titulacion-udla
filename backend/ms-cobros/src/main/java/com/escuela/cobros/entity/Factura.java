package com.escuela.cobros.entity;

import com.escuela.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Factura de un estudiante. {@code saldo} es generated column en BD.
 * Soporta pagos parciales (estado PARCIAL) y optimistic locking (@Version).
 */
@Entity
@Table(name = "facturas", schema = "cobros_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Factura extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_factura", nullable = false, length = 20, unique = true)
    private String numeroFactura;

    @Column(name = "estudiante_id", nullable = false)
    private Long estudianteId;

    @Column(name = "concepto_facturacion_id", nullable = false)
    private Long conceptoFacturacionId;

    @Column(length = 255)
    private String descripcion;

    @Column(name = "monto_original", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoOriginal;

    @Column(name = "monto_pagado", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoPagado = BigDecimal.ZERO;

    /** Generated column: monto_original - monto_pagado. Read-only desde Java. */
    @Column(precision = 10, scale = 2, insertable = false, updatable = false)
    private BigDecimal saldo;

    @Column(nullable = false, length = 20)
    private String estado = "PENDIENTE";

    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision = LocalDate.now();

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Column(name = "motivo_anulacion", columnDefinition = "TEXT")
    private String motivoAnulacion;

    // ============== CRÉDITO (V2) ==============

    /** CONTADO o CREDITO. Si CONTADO → numero_cuotas=1 y sin frecuencia. */
    @Column(name = "tipo_pago", nullable = false, length = 20)
    private String tipoPago = "CONTADO";

    /** 1 si CONTADO, 2..24 si CREDITO. */
    @Column(name = "numero_cuotas", nullable = false)
    private Integer numeroCuotas = 1;

    /** Cuotas saldadas (mantenido por {@code PagoService} cuando registra un pago de crédito). */
    @Column(name = "cuotas_pagadas", nullable = false)
    private Integer cuotasPagadas = 0;

    /** MENSUAL / QUINCENAL / SEMANAL. NULL si CONTADO. */
    @Column(name = "frecuencia_cuota", length = 20)
    private String frecuenciaCuota;

    /** Fecha de la primera cuota (las siguientes se calculan según frecuencia). */
    @Column(name = "fecha_primera_cuota")
    private LocalDate fechaPrimeraCuota;

    /** Persisted: monto_original / numero_cuotas. */
    @Column(name = "valor_cuota", precision = 10, scale = 2)
    private BigDecimal valorCuota;

    @Version
    @Column(nullable = false)
    private Long version = 0L;
}
