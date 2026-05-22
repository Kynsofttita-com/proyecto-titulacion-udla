package com.escuela.estudiantes.controller;

import com.escuela.common.security.headers.UserHeaders;
import com.escuela.estudiantes.dto.ContactoEmergenciaRequest;
import com.escuela.estudiantes.dto.ContactoEmergenciaResponse;
import com.escuela.estudiantes.security.AuthHeaderGuard;
import com.escuela.estudiantes.service.ContactoEmergenciaService;
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
@RequestMapping("/estudiantes/{estudianteId}/contactos-emergencia")
@Tag(name = "Estudiantes - Contactos Emergencia", description = "Contactos de emergencia del estudiante")
public class ContactoEmergenciaController {

    private static final Set<String> ROLES_LECTURA = Set.of("ADMIN", "STAFF", "INSTRUCTOR");
    private static final Set<String> ROLES_ESCRITURA = Set.of("ADMIN", "STAFF");

    private final ContactoEmergenciaService service;

    public ContactoEmergenciaController(ContactoEmergenciaService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar contactos de emergencia del estudiante")
    public ResponseEntity<List<ContactoEmergenciaResponse>> listar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long estudianteId) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_LECTURA);
        return ResponseEntity.ok(service.listar(estudianteId));
    }

    @PostMapping
    @Operation(summary = "Agregar contacto", description = "Si esPrincipal=true, despromueve a otros principales.")
    public ResponseEntity<ContactoEmergenciaResponse> agregar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long estudianteId,
            @Valid @RequestBody ContactoEmergenciaRequest request) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_ESCRITURA);
        ContactoEmergenciaResponse creado = service.agregar(estudianteId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.id()).toUri();
        return ResponseEntity.created(location).body(creado);
    }

    @PutMapping("/{contactoId}")
    @Operation(summary = "Actualizar contacto")
    public ResponseEntity<ContactoEmergenciaResponse> actualizar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long estudianteId,
            @PathVariable Long contactoId,
            @Valid @RequestBody ContactoEmergenciaRequest request) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_ESCRITURA);
        return ResponseEntity.ok(service.actualizar(estudianteId, contactoId, request));
    }

    @DeleteMapping("/{contactoId}")
    @Operation(summary = "Eliminar contacto (soft delete)")
    public ResponseEntity<Void> eliminar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long estudianteId,
            @PathVariable Long contactoId) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_ESCRITURA);
        service.eliminar(estudianteId, contactoId);
        return ResponseEntity.noContent().build();
    }
}
