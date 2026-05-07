---
name: generate-jpa-entity
description: Generate a JPA entity class with proper annotations, audit fields, validation constraints, and matching repository interface. Includes Lombok annotations, EntityListeners for auditing, and follows project naming conventions (Spanish domain terms, snake_case columns). Use when adding a new persistent entity to a microservice.
---

# Generate JPA Entity Skill

Generates a complete JPA entity class with the project's standard structure.

## Inputs Needed

Ask the user for:
1. **Service**: which microservice (e.g., `ms-estudiantes`)
2. **Entity name**: PascalCase singular Spanish noun (e.g., `Estudiante`, `Vehiculo`)
3. **Table name**: snake_case plural (e.g., `estudiantes`, `vehiculos`)
4. **Fields**: list with types and constraints
5. **Relationships**: any `@ManyToOne`, `@OneToMany`, `@ManyToMany`?
6. **Soft delete needed?**: yes/no

## Standard Structure

### Entity Template

```java
package com.kynsoft.<context>.infrastructure.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;

@Entity
@Table(name = "<table_name>", indexes = {
    @Index(name = "idx_<table>_<column>", columnList = "<column>")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
// If soft delete needed:
// @SQLDelete(sql = "UPDATE <table_name> SET deleted_at = NOW() WHERE id = ?")
// @Where(clause = "deleted_at IS NULL")
public class <EntityName> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Domain fields here
    @Column(name = "cedula", nullable = false, unique = true, length = 10)
    @NotBlank
    @Pattern(regexp = "^[0-9]{10}$", message = "Cédula must be 10 digits")
    private String cedula;

    @Column(name = "nombres", nullable = false, length = 100)
    @NotBlank
    @Size(min = 2, max = 100)
    private String nombres;

    // Enum field
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private Estado<Entity> estado;

    // Relationship example: ManyToOne
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id")
    private Instructor instructor;

    // Relationship example: OneToMany
    @OneToMany(mappedBy = "estudiante", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Asignacion> asignaciones = new ArrayList<>();

    // ===== Audit Fields =====

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by", nullable = false, length = 100, updatable = false)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by", nullable = false, length = 100)
    private String updatedBy;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    // If soft delete:
    // @Column(name = "deleted_at")
    // private OffsetDateTime deletedAt;
}
```

### Repository Template

```java
package com.kynsoft.<context>.infrastructure.persistence.repository;

import com.kynsoft.<context>.infrastructure.persistence.entity.<EntityName>;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface <EntityName>Repository 
    extends JpaRepository<<EntityName>, Long>, JpaSpecificationExecutor<<EntityName>> {

    Optional<<EntityName>> findByCedula(String cedula);
    
    boolean existsByCedula(String cedula);
    
    boolean existsByEmail(String email);
}
```

### Audit Configuration (only if not yet exists in service)

```java
package com.kynsoft.<context>.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
@EnableJpaAuditing
public class JpaAuditConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
            .map(auth -> auth.isAuthenticated() ? auth.getName() : "system")
            .or(() -> Optional.of("system"));
    }
}
```

## Workflow

1. **Read** existing entities in the service to match conventions
2. **Generate** the entity class with all fields and validations
3. **Generate** the repository interface
4. **Generate** the Flyway migration matching the entity (delegate to `generate-flyway-migration` skill)
5. **Verify** the entity compiles: `mvn compile`
6. **Verify** Hibernate validation passes: `mvn spring-boot:run` (look for schema validation errors)

## Conventions Reminder

- Table names: snake_case, plural Spanish (e.g., `estudiantes`, `vehiculos`)
- Column names: snake_case (e.g., `fecha_nacimiento`, `tipo_licencia`)
- FK columns: `<entity_singular>_id` (e.g., `instructor_id`)
- Booleans: `es_<adjective>` or `tiene_<noun>`
- Dates: `fecha_<event>` for business dates, `<event>_at` for audit
- Always add `@Version` for optimistic locking on financial entities
- Always use `@Enumerated(EnumType.STRING)` for enums (not ORDINAL — fragile)
- Always specify column length for VARCHAR
- Always add NOT NULL constraints when applicable
- Add indexes on FK columns and frequently-queried fields

## Quality Checklist

- [ ] Entity name in PascalCase, singular
- [ ] Table name in snake_case, plural
- [ ] Audit fields included (created_at, updated_at, created_by, updated_by, version)
- [ ] @Version for optimistic locking
- [ ] @Builder for fluent construction
- [ ] Validation annotations on fields
- [ ] Proper relationship types (LAZY by default)
- [ ] Indexes on FK and queried columns
- [ ] Repository interface generated
- [ ] Migration script generated (use `generate-flyway-migration` skill)
- [ ] Compiles successfully

## Notes

- Use `OffsetDateTime` (not `LocalDateTime`) for timestamps to preserve timezone
- Use `LocalDate` for business dates without time component
- Use `BigDecimal` for money (never `double`/`float`)
- For Spanish enum values, use SCREAMING_SNAKE_CASE (e.g., `EN_PROGRESO`, `RETIRADO`)
