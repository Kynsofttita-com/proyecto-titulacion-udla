package com.escuela.instructores.controller;

import com.escuela.common.security.headers.UserHeaders;
import com.escuela.instructores.dto.CertificacionRequest;
import com.escuela.instructores.dto.CertificacionResponse;
import com.escuela.instructores.security.AuthHeaderGuard;
import com.escuela.instructores.service.CertificacionService;
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
@RequestMapping("/instructores/{instructorId}/certificaciones")
@Tag(name = "Instructores - Certificaciones", description = "Certificaciones y credenciales del instructor")
public class CertificacionController {

    private static final Set<String> ROLES_LECTURA = Set.of("ADMIN", "STAFF", "INSTRUCTOR");
    private static final Set<String> ROLES_ESCRITURA = Set.of("ADMIN", "STAFF");

    private final CertificacionService service;

    public CertificacionController(CertificacionService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar certificaciones del instructor")
    public ResponseEntity<List<CertificacionResponse>> listar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long instructorId) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_LECTURA);
        return ResponseEntity.ok(service.listar(instructorId));
    }

    @PostMapping
    @Operation(summary = "Agregar certificación")
    public ResponseEntity<CertificacionResponse> agregar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long instructorId,
            @Valid @RequestBody CertificacionRequest request) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_ESCRITURA);
        CertificacionResponse creada = service.agregar(instructorId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creada.getId()).toUri();
        return ResponseEntity.created(location).body(creada);
    }

    @PutMapping("/{certificacionId}")
    @Operation(summary = "Actualizar certificación")
    public ResponseEntity<CertificacionResponse> actualizar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long instructorId,
            @PathVariable Long certificacionId,
            @Valid @RequestBody CertificacionRequest request) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_ESCRITURA);
        return ResponseEntity.ok(service.actualizar(instructorId, certificacionId, request));
    }

    @DeleteMapping("/{certificacionId}")
    @Operation(summary = "Eliminar certificación (soft delete)")
    public ResponseEntity<Void> eliminar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long instructorId,
            @PathVariable Long certificacionId) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_ESCRITURA);
        service.eliminar(instructorId, certificacionId);
        return ResponseEntity.noContent().build();
    }
}
