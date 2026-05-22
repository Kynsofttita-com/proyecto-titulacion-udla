package com.escuela.vehiculos.controller;

import com.escuela.common.security.headers.UserHeaders;
import com.escuela.vehiculos.dto.InspeccionRequest;
import com.escuela.vehiculos.dto.InspeccionResponse;
import com.escuela.vehiculos.security.AuthHeaderGuard;
import com.escuela.vehiculos.service.InspeccionService;
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
@RequestMapping("/vehiculos/{vehiculoId}/inspecciones")
@Tag(name = "Vehiculos - Inspecciones", description = "Inspecciones tecnomecanicas, SOAT, etc.")
public class InspeccionController {

    private static final Set<String> ROLES_LECTURA = Set.of("ADMIN", "STAFF", "INSTRUCTOR");
    private static final Set<String> ROLES_ESCRITURA = Set.of("ADMIN", "STAFF");

    private final InspeccionService service;

    public InspeccionController(InspeccionService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar inspecciones del vehiculo")
    public ResponseEntity<List<InspeccionResponse>> listar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long vehiculoId) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_LECTURA);
        return ResponseEntity.ok(service.listar(vehiculoId));
    }

    @PostMapping
    @Operation(summary = "Registrar inspeccion")
    public ResponseEntity<InspeccionResponse> registrar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long vehiculoId,
            @Valid @RequestBody InspeccionRequest request) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_ESCRITURA);
        InspeccionResponse creada = service.registrar(vehiculoId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creada.id()).toUri();
        return ResponseEntity.created(location).body(creada);
    }

    @PutMapping("/{inspeccionId}")
    @Operation(summary = "Actualizar inspeccion")
    public ResponseEntity<InspeccionResponse> actualizar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long vehiculoId,
            @PathVariable Long inspeccionId,
            @Valid @RequestBody InspeccionRequest request) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_ESCRITURA);
        return ResponseEntity.ok(service.actualizar(vehiculoId, inspeccionId, request));
    }

    @DeleteMapping("/{inspeccionId}")
    @Operation(summary = "Eliminar inspeccion (soft delete)")
    public ResponseEntity<Void> eliminar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long vehiculoId,
            @PathVariable Long inspeccionId) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_ESCRITURA);
        service.eliminar(vehiculoId, inspeccionId);
        return ResponseEntity.noContent().build();
    }
}
