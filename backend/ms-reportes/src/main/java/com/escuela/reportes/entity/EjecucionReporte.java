package com.escuela.reportes.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/** Auditoría de ejecuciones de reportes (PDF/Excel) por usuario. */
@Entity
@Table(name = "ejecuciones_reporte", schema = "reportes_schema")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class EjecucionReporte implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo_reporte", nullable = false, length = 50)
    private String tipoReporte;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "JSONB")
    private Map<String, Object> parametros;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "fecha_ejecucion", nullable = false)
    private LocalDateTime fechaEjecucion = LocalDateTime.now();

    @Column(name = "duracion_ms")
    private Integer duracionMs;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(name = "archivo_url", length = 500)
    private String archivoUrl;

    @Column(length = 10)
    private String formato;

    @Column(name = "error_mensaje", columnDefinition = "TEXT")
    private String errorMensaje;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(name = "created_by", length = 50, updatable = false)
    private String createdBy;
}
