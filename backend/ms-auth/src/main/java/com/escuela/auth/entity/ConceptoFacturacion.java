package com.escuela.auth.entity;

import com.escuela.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * Concepto facturable (curso, examen, material, etc.).
 *
 * <p>Catálogo configurable por la escuela. Las facturas en MS-Cobros referencian
 * estos conceptos por ID (sin FK cross-schema).</p>
 */
@Entity
@Table(name = "conceptos_facturacion", schema = "auth_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ConceptoFacturacion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String nombre;

    @Column(name = "monto_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoBase;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false)
    private Boolean activo = Boolean.TRUE;
}
