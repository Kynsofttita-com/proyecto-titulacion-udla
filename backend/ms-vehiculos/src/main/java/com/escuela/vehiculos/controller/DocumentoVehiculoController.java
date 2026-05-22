package com.escuela.vehiculos.controller;

import com.escuela.common.security.headers.UserHeaders;
import com.escuela.vehiculos.dto.DocumentoVehiculoRequest;
import com.escuela.vehiculos.dto.DocumentoVehiculoResponse;
import com.escuela.vehiculos.security.AuthHeaderGuard;
import com.escuela.vehiculos.service.DocumentoVehiculoService;
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
@RequestMapping("/vehiculos/{vehiculoId}/documentos")
@Tag(name = "Vehiculos - Documentos", description = "Documentacion del vehiculo (matricula, SOAT, etc.)")
public class DocumentoVehiculoController {

    private static final Set<String> ROLES_LECTURA = Set.of("ADMIN", "STAFF", "INSTRUCTOR");
    private static final Set<String> ROLES_ESCRITURA = Set.of("ADMIN", "STAFF");

    private final DocumentoVehiculoService service;

    public DocumentoVehiculoController(DocumentoVehiculoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar documentos del vehiculo")
    public ResponseEntity<List<DocumentoVehiculoResponse>> listar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long vehiculoId) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_LECTURA);
        return ResponseEntity.ok(service.listar(vehiculoId));
    }

    @PostMapping
    @Operation(summary = "Subir documento")
    public ResponseEntity<DocumentoVehiculoResponse> subir(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long vehiculoId,
            @Valid @RequestBody DocumentoVehiculoRequest request) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_ESCRITURA);
        DocumentoVehiculoResponse creado = service.subir(vehiculoId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.id()).toUri();
        return ResponseEntity.created(location).body(creado);
    }

    @PutMapping("/{docId}")
    @Operation(summary = "Actualizar documento")
    public ResponseEntity<DocumentoVehiculoResponse> actualizar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long vehiculoId,
            @PathVariable Long docId,
            @Valid @RequestBody DocumentoVehiculoRequest request) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_ESCRITURA);
        return ResponseEntity.ok(service.actualizar(vehiculoId, docId, request));
    }

    @DeleteMapping("/{docId}")
    @Operation(summary = "Eliminar documento (soft delete)")
    public ResponseEntity<Void> eliminar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long vehiculoId,
            @PathVariable Long docId) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_ESCRITURA);
        service.eliminar(vehiculoId, docId);
        return ResponseEntity.noContent().build();
    }
}
