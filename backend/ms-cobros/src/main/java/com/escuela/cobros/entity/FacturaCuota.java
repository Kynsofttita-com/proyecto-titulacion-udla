package com.escuela.cobros.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Cuota individual de una factura a crédito.
 *
 * <p>Solo se materializa cuando {@code facturas.tipo_pago = 'CREDITO'}. Es la
 * fuente de verdad de cuánto debe cada cuota y cuándo vence — los pagos
 * recibidos se aplican a la cuota más antigua pendiente.</p>
 *
 * <p>{@code saldo} es generated column en BD ({@code monto - monto_pagado}).</p>
 */
@Entity
@Table(name = "factura_cuotas", schema = "cobros_schema")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class FacturaCuota implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "factura_id", nullable = false)
    private Long facturaId;

    @Column(name = "numero_cuota", nullable = false)
    private Integer numeroCuota;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(name = "monto_pagado", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoPagado = BigDecimal.ZERO;

    /** Generated column en BD: monto - monto_pagado. Read-only desde Java. */
    @Column(precision = 10, scale = 2, insertable = false, updatable = false)
    private BigDecimal saldo;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Column(name = "fecha_pago_completa")
    private LocalDateTime fechaPagoCompleta;

    @Column(nullable = false, length = 20)
    private String estado = "PENDIENTE";

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
