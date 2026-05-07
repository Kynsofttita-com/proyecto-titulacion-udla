package com.escuela.estudiantes.entity;

import com.escuela.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Estudiante matriculado en la escuela.
 *
 * <p>Las referencias a {@code tipoCursoId}, {@code categoriaLicenciaId} y
 * {@code usuarioId} apuntan a tablas de {@code auth_schema} sin FK cross-schema
 * (consistencia eventual via eventos RabbitMQ).</p>
 */
@Entity
@Table(name = "estudiantes", schema = "estudiantes_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Estudiante extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10, unique = true)
    private String cedula;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(nullable = false, length = 255, unique = true)
    private String email;

    @Column(nullable = false, length = 10)
    private String telefono;

    @Column(columnDefinition = "TEXT")
    private String direccion;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(length = 1)
    private String genero;

    @Column(nullable = false, length = 20)
    private String estado = "PRE_MATRICULADO";

    @Column(name = "fecha_matricula")
    private LocalDate fechaMatricula;

    @Column(name = "tipo_curso_id")
    private Long tipoCursoId;

    @Column(name = "categoria_licencia_id")
    private Long categoriaLicenciaId;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @OneToMany(mappedBy = "estudiante", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @lombok.Builder.Default
    private Set<Documento> documentos = new HashSet<>();

    @OneToMany(mappedBy = "estudiante", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @lombok.Builder.Default
    private Set<ContactoEmergencia> contactosEmergencia = new HashSet<>();
}
