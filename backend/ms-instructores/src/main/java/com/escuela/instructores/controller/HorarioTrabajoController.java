package com.escuela.instructores.controller;

import com.escuela.common.security.headers.UserHeaders;
import com.escuela.instructores.dto.HorarioTrabajoRequest;
import com.escuela.instructores.dto.HorarioTrabajoResponse;
import com.escuela.instructores.security.AuthHeaderGuard;
import com.escuela.instructores.service.HorarioTrabajoService;
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
@RequestMapping("/instructores/{instructorId}/horarios-trabajo")
@Tag(name = "Instructores - Horarios Trabajo",
        description = "Excepciones puntuales a la disponibilidad recurrente (EXTRA/AUSENCIA)")
public class HorarioTrabajoController {

    private static final Set<String> ROLES_LECTURA = Set.of("ADMIN", "STAFF", "INSTRUCTOR");
    private static final Set<String> ROLES_ESCRITURA = Set.of("ADMIN", "STAFF");

    private final HorarioTrabajoService service;

    public HorarioTrabajoController(HorarioTrabajoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar horarios excepcionales del instructor")
    public ResponseEntity<List<HorarioTrabajoResponse>> listar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long instructorId) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_LECTURA);
        return ResponseEntity.ok(service.listar(instructorId));
    }

    @PostMapping
    @Operation(summary = "Registrar excepcion (EXTRA o AUSENCIA)")
    public ResponseEntity<HorarioTrabajoResponse> agregar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long instructorId,
            @Valid @RequestBody HorarioTrabajoRequest request) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_ESCRITURA);
        HorarioTrabajoResponse creado = service.agregar(instructorId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.id()).toUri();
        return ResponseEntity.created(location).body(creado);
    }

    @DeleteMapping("/{horarioId}")
    @Operation(summary = "Eliminar excepcion (soft delete)")
    public ResponseEntity<Void> eliminar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long instructorId,
            @PathVariable Long horarioId) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_ESCRITURA);
        service.eliminar(instructorId, horarioId);
        return ResponseEntity.noContent().build();
    }
}
