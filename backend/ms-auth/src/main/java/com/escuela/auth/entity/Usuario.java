package com.escuela.auth.entity;

import com.escuela.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Usuario del sistema con autenticación JWT.
 *
 * <p>Cada usuario puede tener uno o más {@link Rol}es. La contraseña se almacena
 * con BCrypt (60 caracteres). Soporta lockout automático tras 3 intentos fallidos
 * de login (campo {@code locked} y {@code lockUntil}).</p>
 */
@Entity
@Table(name = "usuarios", schema = "auth_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Usuario extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255, unique = true)
    private String email;

    /** Hash BCrypt de la contraseña (60 caracteres). */
    @Column(nullable = false, length = 60)
    private String password;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(length = 10)
    private String telefono;

    @Column(nullable = false)
    private Boolean activo = Boolean.TRUE;

    @Column(nullable = false)
    private Boolean locked = Boolean.FALSE;

    @Column(name = "failed_attempts", nullable = false)
    private Short failedAttempts = 0;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "lock_until")
    private LocalDateTime lockUntil;

    /**
     * Roles asignados al usuario (relación muchos-a-muchos via {@code usuario_rol}).
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "usuario_rol",
            schema = "auth_schema",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_id")
    )
    @lombok.Builder.Default
    private Set<Rol> roles = new HashSet<>();
}
