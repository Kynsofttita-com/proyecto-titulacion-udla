package com.escuela.vehiculos.controller;

import com.escuela.common.security.headers.UserHeaders;
import com.escuela.vehiculos.dto.CombustibleRequest;
import com.escuela.vehiculos.dto.CombustibleResponse;
import com.escuela.vehiculos.security.AuthHeaderGuard;
import com.escuela.vehiculos.service.CombustibleService;
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
@RequestMapping("/vehiculos/{vehiculoId}/combustible")
@Tag(name = "Vehiculos - Combustible", description = "Registros de carga de combustible")
public class CombustibleController {

    private static final Set<String> ROLES_LECTURA = Set.of("ADMIN", "STAFF", "INSTRUCTOR");
    private static final Set<String> ROLES_ESCRITURA = Set.of("ADMIN", "STAFF", "INSTRUCTOR");
    private static final Set<String> ROLES_BORRADO = Set.of("ADMIN");

    private final CombustibleService service;

    public CombustibleController(CombustibleService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar registros de combustible (paginado, mas reciente primero)")
    public ResponseEntity<Page<CombustibleResponse>> listar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long vehiculoId,
            @PageableDefault(size = 30) Pageable pageable) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_LECTURA);
        return ResponseEntity.ok(service.listar(vehiculoId, pageable));
    }

    @PostMapping
    @Operation(summary = "Registrar carga de combustible",
            description = "INSTRUCTOR puede registrar. Kilometraje debe ser >= al actual del vehiculo.")
    public ResponseEntity<CombustibleResponse> registrar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long vehiculoId,
            @Valid @RequestBody CombustibleRequest request) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_ESCRITURA);
        CombustibleResponse creado = service.registrar(vehiculoId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.id()).toUri();
        return ResponseEntity.created(location).body(creado);
    }

    @DeleteMapping("/{registroId}")
    @Operation(summary = "Eliminar registro (hard delete, solo ADMIN)")
    public ResponseEntity<Void> eliminar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long vehiculoId,
            @PathVariable Long registroId) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_BORRADO);
        service.eliminar(vehiculoId, registroId);
        return ResponseEntity.noContent().build();
    }
}
