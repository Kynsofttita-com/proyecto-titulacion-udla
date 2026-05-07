package com.escuela.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Token único para recuperación de contraseña.
 *
 * <p>Se genera al solicitar "olvidé mi contraseña" y se envía via email. Tiene
 * expiración de 1 hora (configurable). Se invalida al usarse para evitar reuso.</p>
 *
 * <p>NO extiende BaseEntity porque tiene ciclo de vida corto y no soporta
 * soft delete (se borra cuando expira o se usa).</p>
 */
@Entity
@Table(name = "password_reset_token", schema = "auth_schema")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PasswordResetToken implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, unique = true)
    private UUID token;

    @Column(name = "expira_en", nullable = false)
    private LocalDateTime expiraEn;

    @Column(nullable = false)
    private Boolean usado = Boolean.FALSE;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public boolean isValid() {
        return !Boolean.TRUE.equals(usado) && expiraEn.isAfter(LocalDateTime.now());
    }
}
