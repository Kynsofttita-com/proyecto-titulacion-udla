package com.escuela.cobros.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Pago realizado contra una factura.
 *
 * <p>Sin soft delete - los pagos se mantienen permanentemente para auditoría
 * contable. Si hay error, se anula via nota de crédito (no implementada aún).</p>
 */
@Entity
@Table(name = "pagos", schema = "cobros_schema")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Pago implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "factura_id", nullable = false)
    private Factura factura;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(name = "fecha_pago", nullable = false)
    private LocalDateTime fechaPago = LocalDateTime.now();

    @Column(name = "metodo_pago", nullable = false, length = 20)
    private String metodoPago;

    @Column(name = "referencia_transaccion", length = 100)
    private String referenciaTransaccion;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "usuario_registro_id", nullable = false)
    private Long usuarioRegistroId;

    /** Cuota cubierta por este pago (NULL si CONTADO o pago libre). */
    @Column(name = "numero_cuota")
    private Integer numeroCuota;

    /** FK a {@code factura_cuotas.id} cuando el pago se imputa a una cuota. */
    @Column(name = "factura_cuota_id")
    private Long facturaCuotaId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(name = "created_by", length = 50, updatable = false)
    private String createdBy;
}
