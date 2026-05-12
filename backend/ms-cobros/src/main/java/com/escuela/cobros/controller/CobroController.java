package com.escuela.cobros.controller;

import com.escuela.common.security.headers.UserHeaders;
import com.escuela.cobros.dto.CobroListResponse;
import com.escuela.cobros.dto.CobroResponse;
import com.escuela.cobros.dto.CreateCobroRequest;
import com.escuela.cobros.dto.UpdateCobroRequest;
import com.escuela.cobros.service.CobroService;
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
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/cobros")
@Tag(name = "Cobros", description = "Gestión de cobros y pagos")
public class CobroController {

    private static final Set<String> ROLES_ESCRITURA = Set.of("ADMIN", "STAFF");
    private static final Set<String> ROLES_BORRADO = Set.of("ADMIN");

    private final CobroService service;

    public CobroController(CobroService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar cobros")
    public ResponseEntity<Page<CobroListResponse>> listar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @PageableDefault(size = 50, sort = "id") Pageable pageable,
            @RequestParam(required = false) String estado) {
        validarAutenticacion(userEmail);
        return ResponseEntity.ok(service.findAll(pageable, estado));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de un cobro")
    public ResponseEntity<CobroResponse> obtener(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @PathVariable Long id) {
        validarAutenticacion(userEmail);
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    @Operation(summary = "Registrar un cobro", description = "Solo ADMIN o STAFF")
    public ResponseEntity<CobroResponse> crear(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @Valid @RequestBody CreateCobroRequest request) {
        validarAutenticacion(userEmail);
        validarRoles(userRoles, ROLES_ESCRITURA);

        CobroResponse creado = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creado.id())
                .toUri();
        return ResponseEntity.created(location).body(creado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un cobro")
    public ResponseEntity<CobroResponse> actualizar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long id,
            @Valid @RequestBody UpdateCobroRequest request) {
        validarAutenticacion(userEmail);
        validarRoles(userRoles, ROLES_ESCRITURA);
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un cobro (soft delete)", description = "Solo ADMIN")
    public ResponseEntity<Void> eliminar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long id) {
        validarAutenticacion(userEmail);
        validarRoles(userRoles, ROLES_BORRADO);
        service.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    private void validarAutenticacion(String userEmail) {
        if (userEmail == null || userEmail.isBlank()) {
            throw new NoAutenticadoException();
        }
    }

    private void validarRoles(String userRolesHeader, Set<String> rolesPermitidos) {
        if (userRolesHeader == null || userRolesHeader.isBlank()) {
            throw new SinPermisoException();
        }
        List<String> rolesUsuario = Arrays.asList(userRolesHeader.split("\\s*,\\s*"));
        if (!rolesUsuario.stream().anyMatch(rolesPermitidos::contains)) {
            throw new SinPermisoException();
        }
    }

    public static class NoAutenticadoException extends RuntimeException {
        public NoAutenticadoException() {
            super("Token de autenticacion requerido");
        }
    }

    public static class SinPermisoException extends RuntimeException {
        public SinPermisoException() {
            super("El rol del usuario no permite esta operacion");
        }
    }
}
