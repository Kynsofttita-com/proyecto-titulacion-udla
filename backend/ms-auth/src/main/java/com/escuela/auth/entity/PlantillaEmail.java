package com.escuela.auth.entity;

import com.escuela.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

/**
 * Plantilla de email transaccional editable por el ADMIN.
 *
 * <p>El cuerpo HTML usa sintaxis Mustache/Thymeleaf con {@code {{variable}}}
 * reemplazada en runtime al enviar el email. Las variables disponibles se
 * documentan en el campo {@code variables} (JSONB).</p>
 */
@Entity
@Table(name = "plantillas_email", schema = "auth_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PlantillaEmail extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String codigo;

    @Column(nullable = false)
    private String asunto;

    @Column(name = "cuerpo_html", nullable = false, columnDefinition = "TEXT")
    private String cuerpoHtml;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSONB")
    private Map<String, Object> variables;

    @Column(nullable = false)
    private Boolean activa = Boolean.TRUE;
}
