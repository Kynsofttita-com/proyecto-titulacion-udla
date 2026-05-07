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
 * Tipo de curso ofrecido por la escuela (configurable).
 *
 * <p>Ej: "Curso Básico Auto", "Curso Profesional C", "Curso Moto". Cada uno
 * vinculado a una {@link CategoriaLicencia}.</p>
 */
@Entity
@Table(name = "tipos_curso", schema = "auth_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TipoCurso extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "duracion_total_horas", nullable = false)
    private Short duracionTotalHoras;

    @Column(name = "precio_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioBase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_licencia_id")
    private CategoriaLicencia categoriaLicencia;

    @Column(nullable = false)
    private Boolean activo = Boolean.TRUE;
}
