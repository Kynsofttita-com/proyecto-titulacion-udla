package com.escuela.instructores.controller;

import com.escuela.common.security.headers.UserHeaders;
import com.escuela.instructores.dto.DisponibilidadRequest;
import com.escuela.instructores.dto.DisponibilidadResponse;
import com.escuela.instructores.security.AuthHeaderGuard;
import com.escuela.instructores.service.DisponibilidadService;
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
@RequestMapping("/instructores/{instructorId}/disponibilidad-semanal")
@Tag(name = "Instructores - Disponibilidad Semanal", description = "Disponibilidad recurrente semanal del instructor")
public class DisponibilidadController {

    private static final Set<String> ROLES_LECTURA = Set.of("ADMIN", "STAFF", "INSTRUCTOR");
    private static final Set<String> ROLES_ESCRITURA = Set.of("ADMIN", "STAFF");

    private final DisponibilidadService service;

    public DisponibilidadController(DisponibilidadService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar disponibilidad semanal recurrente")
    public ResponseEntity<List<DisponibilidadResponse>> listar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long instructorId) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_LECTURA);
        return ResponseEntity.ok(service.listar(instructorId));
    }

    @PostMapping
    @Operation(summary = "Agregar franja de disponibilidad semanal")
    public ResponseEntity<DisponibilidadResponse> agregar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long instructorId,
            @Valid @RequestBody DisponibilidadRequest request) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_ESCRITURA);
        DisponibilidadResponse creada = service.agregar(instructorId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creada.id()).toUri();
        return ResponseEntity.created(location).body(creada);
    }

    @PutMapping("/{disponibilidadId}")
    @Operation(summary = "Actualizar franja")
    public ResponseEntity<DisponibilidadResponse> actualizar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long instructorId,
            @PathVariable Long disponibilidadId,
            @Valid @RequestBody DisponibilidadRequest request) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_ESCRITURA);
        return ResponseEntity.ok(service.actualizar(instructorId, disponibilidadId, request));
    }

    @DeleteMapping("/{disponibilidadId}")
    @Operation(summary = "Eliminar franja (soft delete)")
    public ResponseEntity<Void> eliminar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long instructorId,
            @PathVariable Long disponibilidadId) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_ESCRITURA);
        service.eliminar(instructorId, disponibilidadId);
        return ResponseEntity.noContent().build();
    }
}
