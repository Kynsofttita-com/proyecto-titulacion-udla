package com.escuela.notificaciones.entity;

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

/** Preferencias de notificación por usuario (1 fila por usuario). */
@Entity
@Table(name = "preferencias_notificacion", schema = "notificaciones_schema")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PreferenciaNotificacion implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false, unique = true)
    private Long usuarioId;

    @Column(name = "recibir_email", nullable = false)
    private Boolean recibirEmail = Boolean.TRUE;

    @Column(name = "recibir_in_app", nullable = false)
    private Boolean recibirInApp = Boolean.TRUE;

    @Column(name = "recibir_recordatorios", nullable = false)
    private Boolean recibirRecordatorios = Boolean.TRUE;

    @Column(name = "recibir_alertas_admin", nullable = false)
    private Boolean recibirAlertasAdmin = Boolean.TRUE;

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
