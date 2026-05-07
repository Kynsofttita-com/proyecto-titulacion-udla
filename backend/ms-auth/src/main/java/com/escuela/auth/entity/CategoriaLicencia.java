package com.escuela.auth.entity;

import com.escuela.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Categoría de licencia de conducir Ecuador (configurable por la escuela).
 *
 * <p>Catálogo del que dependen {@code Vehiculo} y {@code TipoCurso} (referencia
 * por ID, sin FK cross-schema). Ej: A, B, C, D, PROFESIONAL_C, etc.</p>
 */
@Entity
@Table(name = "categorias_licencia", schema = "auth_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CategoriaLicencia extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20, unique = true)
    private String codigo;

    @Column(nullable = false)
    private String descripcion;

    @Column(nullable = false)
    private Boolean activa = Boolean.TRUE;
}
