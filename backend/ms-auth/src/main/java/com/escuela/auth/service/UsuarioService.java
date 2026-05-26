package com.escuela.auth.service;

import com.escuela.auth.dto.AsignarRolesRequest;
import com.escuela.auth.dto.CreateUsuarioRequest;
import com.escuela.auth.dto.UpdateUsuarioRequest;
import com.escuela.auth.dto.UsuarioListResponse;
import com.escuela.auth.dto.UsuarioResponse;
import com.escuela.auth.entity.Rol;
import com.escuela.auth.entity.Usuario;
import com.escuela.auth.exception.DuplicateResourceException;
import com.escuela.auth.exception.ResourceNotFoundException;
import com.escuela.auth.repository.RolRepository;
import com.escuela.auth.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio de gestion de usuarios (admin).
 *
 * <p>Crear/listar/actualizar/desactivar usuarios. Asignacion de roles via
 * tabla {@code usuario_rol}. NUNCA expone el password hash.</p>
 */
@Service
@Transactional
public class UsuarioService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioService.class);

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          RolRepository rolRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Page<UsuarioListResponse> listar(Pageable pageable, Boolean activo, Boolean locked) {
        return usuarioRepository.buscar(activo, locked, pageable).map(this::toListResponse);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id) {
        Usuario u = usuarioRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
        return toResponse(u);
    }

    public UsuarioResponse crear(CreateUsuarioRequest request) {
        usuarioRepository.findByEmailAndDeletedAtIsNull(request.email()).ifPresent(u -> {
            throw new DuplicateResourceException("Ya existe usuario con email " + request.email());
        });

        Set<Rol> roles = resolverRoles(request.roles());

        Usuario u = Usuario.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nombre(request.nombre())
                .apellido(request.apellido())
                .telefono(emptyToNull(request.telefono()))
                .cedula(emptyToNull(request.cedula()))
                .fechaNacimiento(request.fechaNacimiento())
                .genero(emptyToNull(request.genero()))
                .direccion(emptyToNull(request.direccion()))
                .ciudad(emptyToNull(request.ciudad()))
                .provincia(emptyToNull(request.provincia()))
                .passwordChangeRequired(request.passwordChangeRequired() != null ? request.passwordChangeRequired() : Boolean.FALSE)
                .activo(Boolean.TRUE)
                .locked(Boolean.FALSE)
                .failedAttempts((short) 0)
                .roles(roles)
                .build();

        u = usuarioRepository.save(u);
        log.info("Usuario creado id={} email={} roles={}", u.getId(), u.getEmail(), request.roles());
        return toResponse(u);
    }

    public UsuarioResponse actualizar(Long id, UpdateUsuarioRequest request) {
        Usuario u = usuarioRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));

        if (request.nombre() != null) u.setNombre(request.nombre());
        if (request.apellido() != null) u.setApellido(request.apellido());
        if (request.telefono() != null) u.setTelefono(emptyToNull(request.telefono()));
        if (request.cedula() != null) u.setCedula(emptyToNull(request.cedula()));
        if (request.fechaNacimiento() != null) u.setFechaNacimiento(request.fechaNacimiento());
        if (request.genero() != null) u.setGenero(emptyToNull(request.genero()));
        if (request.direccion() != null) u.setDireccion(emptyToNull(request.direccion()));
        if (request.ciudad() != null) u.setCiudad(emptyToNull(request.ciudad()));
        if (request.provincia() != null) u.setProvincia(emptyToNull(request.provincia()));
        if (request.activo() != null) u.setActivo(request.activo());
        if (request.passwordChangeRequired() != null) {
            u.setPasswordChangeRequired(request.passwordChangeRequired());
        }
        if (request.roles() != null && !request.roles().isEmpty()) {
            Set<Rol> roles = resolverRoles(request.roles());
            u.setRoles(roles);
        }

        usuarioRepository.save(u);
        log.info("Usuario actualizado id={}", id);
        return toResponse(u);
    }

    public UsuarioResponse asignarRoles(Long id, AsignarRolesRequest request) {
        Usuario u = usuarioRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));

        Set<Rol> roles = resolverRoles(request.roles());
        u.setRoles(roles);

        usuarioRepository.save(u);
        log.info("Usuario id={} roles actualizados a {}", id, request.roles());
        return toResponse(u);
    }

    public UsuarioResponse desbloquear(Long id) {
        Usuario u = usuarioRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
        u.setLocked(Boolean.FALSE);
        u.setLockUntil(null);
        u.setFailedAttempts((short) 0);
        usuarioRepository.save(u);
        log.info("Usuario id={} desbloqueado por admin", id);
        return toResponse(u);
    }

    public void eliminar(Long id) {
        Usuario u = usuarioRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
        u.setActivo(Boolean.FALSE);
        u.setDeletedAt(LocalDateTime.now());
        usuarioRepository.save(u);
        log.info("Usuario soft-deleted id={}", id);
    }

    public UsuarioResponse cambiarPasswordAdmin(Long id, String nuevaPassword, Boolean passwordChangeRequired) {
        Usuario u = usuarioRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));

        u.setPassword(passwordEncoder.encode(nuevaPassword));
        if (passwordChangeRequired != null) {
            u.setPasswordChangeRequired(passwordChangeRequired);
        }

        usuarioRepository.save(u);
        log.info("Contraseña cambiada por admin para usuario id={}", id);
        return toResponse(u);
    }

    public UsuarioResponse resetearIntentos(Long id) {
        Usuario u = usuarioRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));

        u.setFailedAttempts((short) 0);
        usuarioRepository.save(u);
        log.info("Intentos fallidos reseteados para usuario id={}", id);
        return toResponse(u);
    }

    // ----- helpers -----

    private Set<Rol> resolverRoles(List<String> nombres) {
        if (nombres == null || nombres.isEmpty()) {
            throw new IllegalArgumentException("Debe especificarse al menos un rol");
        }
        List<Rol> encontrados = rolRepository.findByNombreIn(nombres);
        if (encontrados.size() != nombres.size()) {
            List<String> noEncontrados = nombres.stream()
                    .filter(n -> encontrados.stream().noneMatch(r -> r.getNombre().equalsIgnoreCase(n)))
                    .toList();
            throw new ResourceNotFoundException("Rol",
                    noEncontrados.isEmpty() ? nombres : noEncontrados);
        }
        return new HashSet<>(encontrados);
    }

    private UsuarioListResponse toListResponse(Usuario u) {
        return new UsuarioListResponse(
                u.getId(), u.getEmail(), u.getNombre(), u.getApellido(),
                u.getActivo(), u.getLocked(), u.getLastLogin(),
                u.getRoles().stream().map(Rol::getNombre).sorted().toList(),
                u.getTelefono(), u.getCedula(), u.getFechaNacimiento(),
                u.getGenero(), u.getDireccion(), u.getCiudad(), u.getProvincia()
        );
    }

    private UsuarioResponse toResponse(Usuario u) {
        return new UsuarioResponse(
                u.getId(), u.getEmail(), u.getNombre(), u.getApellido(),
                u.getTelefono(), u.getActivo(), u.getLocked(),
                u.getFailedAttempts(), u.getLockUntil(), u.getLastLogin(),
                u.getRoles().stream().map(Rol::getNombre).sorted().toList(),
                u.getCedula(), u.getFechaNacimiento(), u.getGenero(),
                u.getDireccion(), u.getCiudad(), u.getProvincia(),
                u.getPasswordChangeRequired()
        );
    }

    private String emptyToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
