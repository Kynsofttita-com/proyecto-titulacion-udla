package com.escuela.auth.service;

import com.escuela.auth.dto.AsignarRolesRequest;
import com.escuela.auth.dto.CreateUsuarioRequest;
import com.escuela.auth.dto.UpdateUsuarioRequest;
import com.escuela.auth.dto.UsuarioResponse;
import com.escuela.auth.entity.Rol;
import com.escuela.auth.entity.Usuario;
import com.escuela.auth.exception.DuplicateResourceException;
import com.escuela.auth.exception.ResourceNotFoundException;
import com.escuela.auth.repository.RolRepository;
import com.escuela.auth.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private RolRepository rolRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService service;

    private Rol rolAdmin;
    private Rol rolStaff;

    @BeforeEach
    void setUp() {
        rolAdmin = Rol.builder().id(1L).nombre("ADMIN").build();
        rolStaff = Rol.builder().id(2L).nombre("STAFF").build();
    }

    @Test
    @DisplayName("crear() encripta password, asigna roles y devuelve response sin password")
    void crearOk() {
        CreateUsuarioRequest req = new CreateUsuarioRequest(
                "nuevo@escuela.local", "Admin123!", "Juan", "Perez", "0991234567",
                List.of("ADMIN", "STAFF"), null, null, null, null, null, null, true);

        when(usuarioRepository.findByEmail("nuevo@escuela.local"))
                .thenReturn(Optional.empty());
        when(rolRepository.findByNombreIn(List.of("ADMIN", "STAFF")))
                .thenReturn(List.of(rolAdmin, rolStaff));
        when(passwordEncoder.encode("Admin123!")).thenReturn("$2b$10$hash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(99L);
            return u;
        });

        UsuarioResponse response = service.crear(req);

        assertEquals(99L, response.id());
        assertEquals("nuevo@escuela.local", response.email());
        assertTrue(response.roles().contains("ADMIN"));
        assertTrue(response.roles().contains("STAFF"));
        verify(passwordEncoder).encode("Admin123!");
    }

    @Test
    @DisplayName("crear() falla con DuplicateResource si email ya existe")
    void crearDuplicado() {
        CreateUsuarioRequest req = new CreateUsuarioRequest(
                "admin@escuela.local", "Admin123!", "X", "Y", null, List.of("ADMIN"), null, null, null, null, null, null, true);

        when(usuarioRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(Usuario.builder().id(1L).build()));

        assertThrows(DuplicateResourceException.class, () -> service.crear(req));
    }

    @Test
    @DisplayName("crear() falla si algun rol no existe")
    void crearRolNoExiste() {
        CreateUsuarioRequest req = new CreateUsuarioRequest(
                "x@y.com", "Admin123!", "X", "Y", null, List.of("ADMIN", "INEXISTENTE"), null, null, null, null, null, null, true);

        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(rolRepository.findByNombreIn(any())).thenReturn(List.of(rolAdmin));

        assertThrows(ResourceNotFoundException.class, () -> service.crear(req));
    }

    @Test
    @DisplayName("obtener() devuelve usuario si existe")
    void obtenerOk() {
        Usuario u = Usuario.builder()
                .id(5L).email("a@b.com").nombre("A").apellido("B")
                .activo(true).locked(false).failedAttempts((short) 0)
                .roles(Set.of(rolAdmin))
                .build();
        when(usuarioRepository.findByIdAndDeletedAtIsNull(5L)).thenReturn(Optional.of(u));

        UsuarioResponse r = service.obtener(5L);
        assertEquals("a@b.com", r.email());
        assertTrue(r.roles().contains("ADMIN"));
    }

    @Test
    @DisplayName("obtener() falla 404 si id no existe")
    void obtenerNotFound() {
        when(usuarioRepository.findByIdAndDeletedAtIsNull(anyLong())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.obtener(123L));
    }

    @Test
    @DisplayName("actualizar() mezcla solo campos no-null")
    void actualizarParcial() {
        Usuario u = Usuario.builder()
                .id(1L).email("x@y.com").nombre("OldN").apellido("OldA")
                .activo(true).locked(false).failedAttempts((short) 0)
                .roles(new HashSet<>(List.of(rolAdmin)))
                .build();
        when(usuarioRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(u));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateUsuarioRequest req = new UpdateUsuarioRequest("NewName", null, null, null, null, null, null, null, null, null, null, null, null);
        UsuarioResponse r = service.actualizar(1L, req);

        assertEquals("NewName", r.nombre());
        assertEquals("OldA", r.apellido()); // no se modifico
    }

    @Test
    @DisplayName("asignarRoles() reemplaza el set completo de roles")
    void asignarRolesReemplaza() {
        Usuario u = Usuario.builder()
                .id(1L).email("x@y.com").nombre("X").apellido("Y")
                .activo(true).locked(false).failedAttempts((short) 0)
                .roles(new HashSet<>(List.of(rolAdmin)))
                .build();
        when(usuarioRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(u));
        when(rolRepository.findByNombreIn(List.of("STAFF"))).thenReturn(List.of(rolStaff));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioResponse r = service.asignarRoles(1L, new AsignarRolesRequest(List.of("STAFF")));

        assertEquals(1, r.roles().size());
        assertTrue(r.roles().contains("STAFF"));
        assertFalse(r.roles().contains("ADMIN"));
    }

    @Test
    @DisplayName("desbloquear() resetea locked/lockUntil/failedAttempts")
    void desbloquearOk() {
        Usuario u = Usuario.builder()
                .id(1L).email("x@y.com").nombre("X").apellido("Y")
                .activo(true).locked(true).failedAttempts((short) 3)
                .lockUntil(java.time.LocalDateTime.now().plusMinutes(15))
                .roles(new HashSet<>())
                .build();
        when(usuarioRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(u));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioResponse r = service.desbloquear(1L);

        assertFalse(r.locked());
        assertNull(r.lockUntil());
        assertEquals((short) 0, r.failedAttempts());
    }

    @Test
    @DisplayName("eliminar() hace soft delete y marca activo=false")
    void eliminarSoftDelete() {
        Usuario u = Usuario.builder()
                .id(1L).email("x@y.com").nombre("X").apellido("Y")
                .activo(true).locked(false).failedAttempts((short) 0)
                .roles(new HashSet<>())
                .build();
        when(usuarioRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(u));

        service.eliminar(1L);

        assertFalse(u.getActivo());
        assertNotNull(u.getDeletedAt());
        verify(usuarioRepository).save(u);
    }
}
