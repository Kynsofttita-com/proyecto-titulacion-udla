package com.escuela.estudiantes.entity;

import com.escuela.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Documento subido por un estudiante (cédula, foto, examen médico, etc.).
 * El archivo físico se almacena en MinIO; aquí guardamos solo la URL.
 */
@Entity
@Table(name = "documentos", schema = "estudiantes_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Documento extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    @Column(nullable = false, length = 30)
    private String tipo;

    @Column(name = "url_archivo", nullable = false, length = 500)
    private String urlArchivo;

    @Column(name = "fecha_subida", nullable = false)
    private LocalDateTime fechaSubida = LocalDateTime.now();

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "tamano_bytes")
    private Long tamanoBytes;
}
