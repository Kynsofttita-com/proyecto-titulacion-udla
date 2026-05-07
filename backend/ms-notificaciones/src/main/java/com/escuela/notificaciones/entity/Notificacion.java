package com.escuela.notificaciones.entity;

import com.escuela.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

/** Notificación in-app dirigida a un usuario específico. */
@Entity
@Table(name = "notificaciones", schema = "notificaciones_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Notificacion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(nullable = false, length = 50)
    private String tipo;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @Column(nullable = false)
    private Boolean leida = Boolean.FALSE;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_lectura")
    private LocalDateTime fechaLectura;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "datos_extra", columnDefinition = "JSONB")
    private Map<String, Object> datosExtra;

    @Column(nullable = false, length = 10)
    private String prioridad = "NORMAL";
}
