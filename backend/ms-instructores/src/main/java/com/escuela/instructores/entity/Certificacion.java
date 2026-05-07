package com.escuela.instructores.entity;

import com.escuela.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Table(name = "certificaciones", schema = "instructores_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Certificacion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instructor_id", nullable = false)
    private Instructor instructor;

    @Column(nullable = false, length = 100)
    private String tipo;

    @Column(name = "fecha_obtencion", nullable = false)
    private LocalDate fechaObtencion;

    @Column(name = "vigencia_hasta")
    private LocalDate vigenciaHasta;

    @Column(name = "entidad_emisora")
    private String entidadEmisora;

    @Column(name = "archivo_url", length = 500)
    private String archivoUrl;

    @Column(columnDefinition = "TEXT")
    private String observaciones;
}
