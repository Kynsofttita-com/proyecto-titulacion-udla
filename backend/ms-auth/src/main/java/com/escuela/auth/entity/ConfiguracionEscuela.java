package com.escuela.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Configuración de la escuela (single-tenant).
 *
 * <p>Contiene una sola fila (garantizado por trigger en BD). Almacena los
 * parámetros configurables del sistema: datos de la escuela, branding, horarios,
 * tiempos de recordatorios, etc.</p>
 *
 * <p>NO extiende {@link com.escuela.common.jpa.BaseEntity} porque no tiene
 * {@code deleted_at} (la fila siempre existe).</p>
 */
@Entity
@Table(name = "configuracion_escuela", schema = "auth_schema")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ConfiguracionEscuela implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, length = 13, unique = true)
    private String ruc;

    @Column(columnDefinition = "TEXT")
    private String direccion;

    @Column(length = 20)
    private String telefono;

    private String email;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "color_primario", length = 7)
    private String colorPrimario;

    @Column(name = "color_secundario", length = 7)
    private String colorSecundario;

    @Column(name = "duracion_clase_default_min", nullable = false)
    private Short duracionClaseDefaultMin = 60;

    @Column(name = "horario_apertura")
    private LocalTime horarioApertura;

    @Column(name = "horario_cierre")
    private LocalTime horarioCierre;

    @Column(name = "horas_recordatorio_clase", nullable = false)
    private Short horasRecordatorioClase = 24;

    @Column(name = "dias_alerta_soat", nullable = false)
    private Short diasAlertaSoat = 30;

    @Column(name = "max_intentos_fallidos", nullable = false)
    private Short maxIntentosFallidos = 3;

    @Column(name = "duracion_bloqueo_minutos", nullable = false)
    private Short duracionBloqueoMinutos = 15;

    @Column(name = "expiracion_token_reset_minutos", nullable = false)
    private Short expiracionTokenResetMinutos = 60;

    // ----- Audit fields (sin deleted_at) -----

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by", length = 50, updatable = false)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by", length = 50)
    private String updatedBy;
}
