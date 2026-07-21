package com.escuela.auth.service;

import com.escuela.auth.entity.Rol;
import com.escuela.auth.entity.Usuario;
import com.escuela.auth.repository.RolRepository;
import com.escuela.auth.repository.UsuarioRepository;
import com.escuela.common.events.auth.UsuarioCreadoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Crea automaticamente un Usuario en MS-Auth a partir de un evento de dominio
 * (EstudianteCreado / InstructorCreado) y publica {@link UsuarioCreadoEvent}.
 *
 * <p>El password se genera aleatorio (256 bits) y se hashea con BCrypt. El
 * usuario queda inactivo para login normal hasta que solicite reset de password
 * (flujo {@code /auth/forgot-password}).</p>
 */
@Service
public class AutoUsuarioCreator {

    private static final Logger log = LoggerFactory.getLogger(AutoUsuarioCreator.class);
    private static final SecureRandom RNG = new SecureRandom();

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthEventDispatcher eventDispatcher;

    public AutoUsuarioCreator(UsuarioRepository usuarioRepository,
                              RolRepository rolRepository,
                              PasswordEncoder passwordEncoder,
                              AuthEventDispatcher eventDispatcher) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventDispatcher = eventDispatcher;
    }

    /**
     * Crea, reactiva o devuelve el Usuario para el email dado y publica el evento.
     * Idempotente:
     * <ul>
     *   <li>Si existe Usuario activo con ese email: se reusa.</li>
     *   <li>Si existe Usuario soft-deleted (baja previa por eliminar estudiante/instructor):
     *       se REACTIVA con los nuevos datos y el rol especificado. Esto evita chocar con
     *       el UNIQUE global de email y da UX natural: recrear un estudiante con el mismo
     *       email reactiva su usuario del sistema.</li>
     *   <li>Si no existe: se crea uno nuevo.</li>
     * </ul>
     */
    @Transactional
    public void crearYNotificar(String email, String nombre, String apellido, String rolNombre) {
        if (email == null || email.isBlank()) {
            log.warn("Email vacio, skip creacion automatica");
            return;
        }

        Usuario usuario = usuarioRepository.findByEmail(email)
                .map(existente -> existente.getDeletedAt() != null
                        ? reactivar(existente, nombre, apellido, rolNombre)
                        : existente)
                .orElseGet(() -> crearNuevo(email, nombre, apellido, rolNombre));

        eventDispatcher.publishUsuarioCreado(UsuarioCreadoEvent.builder()
                .usuarioId(usuario.getId())
                .email(usuario.getEmail())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .build());
    }

    private Usuario reactivar(Usuario u, String nombre, String apellido, String rolNombre) {
        Rol rol = rolRepository.findByNombre(rolNombre)
                .orElseThrow(() -> new IllegalStateException(
                        "Rol " + rolNombre + " no existe; revisar seed_data."));
        u.setActivo(Boolean.TRUE);
        u.setDeletedAt(null);
        u.setLocked(Boolean.FALSE);
        u.setLockUntil(null);
        u.setFailedAttempts((short) 0);
        u.setNombre(nullSafe(nombre));
        u.setApellido(nullSafe(apellido));
        // Password reset requerido para que el usuario reactivado defina uno nuevo.
        u.setPassword(passwordEncoder.encode(generarPasswordRandom()));
        u.setPasswordChangeRequired(Boolean.TRUE);
        // Sumamos el rol si no lo tenia; conservamos los otros roles previos.
        if (u.getRoles().stream().noneMatch(r -> r.getNombre().equalsIgnoreCase(rolNombre))) {
            u.getRoles().add(rol);
        }
        Usuario saved = usuarioRepository.save(u);
        log.info("Usuario reactivado id={} email={} rol={}", saved.getId(), saved.getEmail(), rolNombre);
        return saved;
    }

    private Usuario crearNuevo(String email, String nombre, String apellido, String rolNombre) {
        Rol rol = rolRepository.findByNombre(rolNombre)
                .orElseThrow(() -> new IllegalStateException(
                        "Rol " + rolNombre + " no existe; revisar seed_data."));
        Set<Rol> roles = new HashSet<>();
        roles.add(rol);

        Usuario u = Usuario.builder()
                .email(email)
                .password(passwordEncoder.encode(generarPasswordRandom()))
                .nombre(nullSafe(nombre))
                .apellido(nullSafe(apellido))
                .activo(Boolean.TRUE)
                .locked(Boolean.FALSE)
                .failedAttempts((short) 0)
                .passwordChangeRequired(Boolean.TRUE)
                .roles(roles)
                .build();
        u = usuarioRepository.save(u);
        log.info("Usuario auto-creado id={} email={} rol={}", u.getId(), email, rolNombre);
        return u;
    }

    private String generarPasswordRandom() {
        byte[] bytes = new byte[32];
        RNG.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String nullSafe(String s) {
        return s == null || s.isBlank() ? "(sin definir)" : s;
    }
}
