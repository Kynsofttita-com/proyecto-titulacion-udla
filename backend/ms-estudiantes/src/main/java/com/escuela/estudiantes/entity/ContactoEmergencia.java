package com.escuela.estudiantes.entity;

import com.escuela.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "contactos_emergencia", schema = "estudiantes_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ContactoEmergencia extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(nullable = false, length = 15)
    private String telefono;

    @Column(length = 50)
    private String parentesco;

    @Column(name = "es_principal", nullable = false)
    private Boolean esPrincipal = Boolean.FALSE;
}
