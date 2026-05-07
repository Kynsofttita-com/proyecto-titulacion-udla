package com.escuela.cobros.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Cierre diario de caja. Una fila por día.
 * {@code totalDia} es generated column en BD (suma de los 4 métodos de pago).
 */
@Entity
@Table(name = "reconciliacion", schema = "cobros_schema")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Reconciliacion implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalDate fecha;

    @Column(name = "total_efectivo", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalEfectivo = BigDecimal.ZERO;

    @Column(name = "total_tarjeta", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalTarjeta = BigDecimal.ZERO;

    @Column(name = "total_transferencia", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalTransferencia = BigDecimal.ZERO;

    @Column(name = "total_cheque", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalCheque = BigDecimal.ZERO;

    /** Generated column: suma de los 4 métodos. Read-only desde Java. */
    @Column(name = "total_dia", precision = 10, scale = 2, insertable = false, updatable = false)
    private BigDecimal totalDia;

    @Column(name = "cantidad_pagos", nullable = false)
    private Integer cantidadPagos = 0;

    @Column(name = "usuario_conciliador_id")
    private Long usuarioConciliadorId;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by", length = 50, updatable = false)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by", length = 50)
    private String updatedBy;
}
