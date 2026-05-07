package com.escuela.auth.entity;

import com.escuela.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

/**
 * Rol del sistema (ADMIN, STAFF, INSTRUCTOR, ESTUDIANTE).
 *
 * <p>Cada rol agrupa un conjunto de {@link Permiso}s. Los usuarios pueden tener
 * uno o más roles. La autorización se hace via los permisos del rol.</p>
 */
@Entity
@Table(name = "roles", schema = "auth_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Rol extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    /**
     * Permisos asignados a este rol (relación muchos-a-muchos via {@code rol_permiso}).
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "rol_permiso",
            schema = "auth_schema",
            joinColumns = @JoinColumn(name = "rol_id"),
            inverseJoinColumns = @JoinColumn(name = "permiso_id")
    )
    @lombok.Builder.Default
    private Set<Permiso> permisos = new HashSet<>();
}
