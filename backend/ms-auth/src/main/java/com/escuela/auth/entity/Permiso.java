package com.escuela.auth.entity;

import com.escuela.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Permiso granular del sistema (ej: ESTUDIANTES_READ, COBROS_WRITE).
 *
 * <p>Asignado a {@link Rol} via la tabla junction {@code rol_permiso}. Validado
 * por Spring Security con {@code @PreAuthorize("hasAuthority('codigo')")}.</p>
 */
@Entity
@Table(name = "permisos", schema = "auth_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Permiso extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String codigo;

    @Column(nullable = false, length = 50)
    private String recurso;

    @Column(nullable = false, length = 50)
    private String accion;

    private String descripcion;
}
