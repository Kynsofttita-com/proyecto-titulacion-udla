package com.escuela.common.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
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

/**
 * Superclase abstracta para todas las entidades del dominio que requieran
 * auditoría automática y soft delete.
 *
 * <p>Provee los siguientes campos audit (manejados automáticamente por Spring
 * Data JPA + {@link AuditingEntityListener}):</p>
 * <ul>
 *   <li>{@code createdAt} - timestamp de creación, no se modifica</li>
 *   <li>{@code updatedAt} - timestamp de última modificación</li>
 *   <li>{@code createdBy} - usuario que creó el registro (de SecurityContext)</li>
 *   <li>{@code updatedBy} - usuario de la última modificación</li>
 * </ul>
 *
 * <p>Y soporte de soft delete:</p>
 * <ul>
 *   <li>{@code deletedAt} - NULL = activo, no NULL = borrado lógicamente</li>
 * </ul>
 *
 * <p>La política del proyecto (ver DECISIONES.md sección 11) es soft delete en
 * todas las entidades EXCEPTO {@code pagos} y {@code audit_log}, que mantienen
 * histórico permanente.</p>
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity implements Serializable {

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

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * Indica si el registro está marcado como borrado (soft delete).
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * Marca el registro como borrado (soft delete) seteando deletedAt = now().
     */
    public void markAsDeleted() {
        this.deletedAt = LocalDateTime.now();
    }
}
