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
 * Movimiento contable: registro real de un ingreso o gasto.
 * <p>Puede ser creado manualmente (gasto administrativo) o generado
 * automaticamente cuando se registra un pago de estudiante (via {@code pagoId}).
 * <p>La anulacion es logica ({@code anulado=true}) para preservar la historia.
 */
@Entity
@Table(name = "movimientos_contables", schema = "contabilidad_schema")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class MovimientoContable implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate fecha;

    /** INGRESO / GASTO */
    @Column(nullable = false, length = 10)
    private String tipo;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cuenta_id", nullable = false)
    private CuentaContable cuenta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private CategoriaMovimiento categoria;

    @Column(length = 255)
    private String descripcion;

    @Column(length = 80)
    private String referencia;

    /** FK opcional a {@code cobros_schema.pagos.id} cuando se auto-genera desde un pago. */
    @Column(name = "pago_id")
    private Long pagoId;

    @Column(nullable = false)
    private Boolean anulado = false;

    @Column(name = "motivo_anulacion", columnDefinition = "TEXT")
    private String motivoAnulacion;

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
