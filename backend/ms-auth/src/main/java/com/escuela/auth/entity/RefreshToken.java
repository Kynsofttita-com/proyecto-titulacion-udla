package com.escuela.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad mapeando {@code auth_schema.refresh_tokens}.
 *
 * <p>Representa un refresh token emitido por MS-Auth. Soporta rotation:
 * cuando se usa para obtener un nuevo access token, se marca como revocado
 * (no se borra para auditoria) y se emite un refresh nuevo.</p>
 */
@Entity
@Table(name = "refresh_tokens", schema = "auth_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "jti", nullable = false, unique = true)
    private UUID jti;

    @Column(name = "expira_en", nullable = false)
    private LocalDateTime expiraEn;

    @Column(name = "revocado", nullable = false)
    @Builder.Default
    private boolean revocado = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "revocado_at")
    private LocalDateTime revocadoAt;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiraEn);
    }

    public boolean isUsable() {
        return !revocado && !isExpired();
    }

    public void revocar() {
        this.revocado = true;
        this.revocadoAt = LocalDateTime.now();
    }

    public static LocalDateTime instantToLocal(Instant instant) {
        return LocalDateTime.ofInstant(instant, java.time.ZoneOffset.UTC);
    }
}
