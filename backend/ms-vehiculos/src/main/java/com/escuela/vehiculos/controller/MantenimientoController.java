package com.escuela.vehiculos.controller;

import com.escuela.common.security.headers.UserHeaders;
import com.escuela.vehiculos.dto.MantenimientoRequest;
import com.escuela.vehiculos.dto.MantenimientoResponse;
import com.escuela.vehiculos.security.AuthHeaderGuard;
import com.escuela.vehiculos.service.MantenimientoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/vehiculos/{vehiculoId}/mantenimientos")
@Tag(name = "Vehiculos - Mantenimientos", description = "Mantenimientos preventivos y correctivos")
public class MantenimientoController {

    private static final Set<String> ROLES_LECTURA = Set.of("ADMIN", "STAFF", "INSTRUCTOR");
    private static final Set<String> ROLES_ESCRITURA = Set.of("ADMIN", "STAFF");

    private final MantenimientoService service;

    public MantenimientoController(MantenimientoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar mantenimientos del vehiculo")
    public ResponseEntity<List<MantenimientoResponse>> listar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long vehiculoId) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_LECTURA);
        return ResponseEntity.ok(service.listar(vehiculoId));
    }

    @PostMapping
    @Operation(summary = "Registrar mantenimiento")
    public ResponseEntity<MantenimientoResponse> registrar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long vehiculoId,
            @Valid @RequestBody MantenimientoRequest request) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_ESCRITURA);
        MantenimientoResponse creado = service.registrar(vehiculoId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.id()).toUri();
        return ResponseEntity.created(location).body(creado);
    }

    @PutMapping("/{mantId}")
    @Operation(summary = "Actualizar mantenimiento")
    public ResponseEntity<MantenimientoResponse> actualizar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long vehiculoId,
            @PathVariable Long mantId,
            @Valid @RequestBody MantenimientoRequest request) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_ESCRITURA);
        return ResponseEntity.ok(service.actualizar(vehiculoId, mantId, request));
    }

    @DeleteMapping("/{mantId}")
    @Operation(summary = "Eliminar mantenimiento (soft delete)")
    public ResponseEntity<Void> eliminar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long vehiculoId,
            @PathVariable Long mantId) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_ESCRITURA);
        service.eliminar(vehiculoId, mantId);
        return ResponseEntity.noContent().build();
    }
}
