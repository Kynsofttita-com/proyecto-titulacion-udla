package com.escuela.auth.controller;

import com.escuela.auth.dto.AsignarRolesRequest;
import com.escuela.auth.dto.CambiarPasswordAdminRequest;
import com.escuela.auth.dto.CreateUsuarioRequest;
import com.escuela.auth.dto.UpdateUsuarioRequest;
import com.escuela.auth.dto.UsuarioListResponse;
import com.escuela.auth.dto.UsuarioResponse;
import com.escuela.auth.security.AuthHeaderGuard;
import com.escuela.auth.service.UsuarioService;
import com.escuela.common.security.headers.UserHeaders;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Set;

@RestController
@RequestMapping("/usuarios")
@Tag(name = "Usuarios", description = "Gestion de usuarios del sistema (admin)")
public class UsuarioController {

    private static final Set<String> ROLES_ADMIN = Set.of("ADMIN");
    private static final Set<String> ROLES_ADMIN_STAFF = Set.of("ADMIN", "STAFF");

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar usuarios", description = "Solo ADMIN")
    public ResponseEntity<Page<UsuarioListResponse>> listar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PageableDefault(size = 50, sort = "id") Pageable pageable,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) Boolean locked) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_ADMIN);
        return ResponseEntity.ok(service.listar(pageable, activo, locked));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por id", description = "Solo ADMIN")
    public ResponseEntity<UsuarioResponse> obtener(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long id) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_ADMIN);
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    @Operation(summary = "Crear usuario nuevo", description = "ADMIN o STAFF")
    public ResponseEntity<UsuarioResponse> crear(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @Valid @RequestBody CreateUsuarioRequest request) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_ADMIN_STAFF);
        UsuarioResponse creado = service.crear(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.id()).toUri();
        return ResponseEntity.created(location).body(creado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar datos del usuario", description = "Solo ADMIN")
    public ResponseEntity<UsuarioResponse> actualizar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long id,
            @Valid @RequestBody UpdateUsuarioRequest request) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_ADMIN);
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @PutMapping("/{id}/roles")
    @Operation(summary = "Reemplazar roles del usuario", description = "Solo ADMIN")
    public ResponseEntity<UsuarioResponse> asignarRoles(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long id,
            @Valid @RequestBody AsignarRolesRequest request) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_ADMIN);
        return ResponseEntity.ok(service.asignarRoles(id, request));
    }

    @PostMapping("/{id}/desbloquear")
    @Operation(summary = "Desbloquear cuenta tras lockout", description = "Solo ADMIN")
    public ResponseEntity<UsuarioResponse> desbloquear(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long id) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_ADMIN);
        return ResponseEntity.ok(service.desbloquear(id));
    }

    @PostMapping("/{id}/resetear-intentos")
    @Operation(summary = "Resetear intentos fallidos", description = "Solo ADMIN")
    public ResponseEntity<UsuarioResponse> resetearIntentos(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long id) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_ADMIN);
        return ResponseEntity.ok(service.resetearIntentos(id));
    }

    @PostMapping("/{id}/cambiar-password")
    @Operation(summary = "Cambiar contraseña como admin", description = "Solo ADMIN")
    public ResponseEntity<UsuarioResponse> cambiarPasswordAdmin(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long id,
            @Valid @RequestBody CambiarPasswordAdminRequest request) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_ADMIN);
        return ResponseEntity.ok(service.cambiarPasswordAdmin(id, request.nuevaPassword(), request.passwordChangeRequired()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario (soft delete)", description = "Solo ADMIN")
    public ResponseEntity<Void> eliminar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long id) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_ADMIN);
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
