package com.escuela.estudiantes.controller;

import com.escuela.common.security.headers.UserHeaders;
import com.escuela.estudiantes.dto.AsistenciaResponse;
import com.escuela.estudiantes.dto.CreateAsistenciaRequest;
import com.escuela.estudiantes.dto.UpdateAsistenciaRequest;
import com.escuela.estudiantes.security.AuthHeaderGuard;
import com.escuela.estudiantes.service.AsistenciaService;
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
@RequestMapping("/estudiantes/{estudianteId}/asistencia")
@Tag(name = "Estudiantes - Asistencia", description = "Asistencia a clases del estudiante")
public class AsistenciaController {

    private static final Set<String> ROLES_LECTURA = Set.of("ADMIN", "STAFF", "INSTRUCTOR", "ESTUDIANTE");
    private static final Set<String> ROLES_REGISTRO = Set.of("ADMIN", "STAFF", "INSTRUCTOR");

    private final AsistenciaService service;

    public AsistenciaController(AsistenciaService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar asistencia del estudiante (paginada, mas reciente primero)")
    public ResponseEntity<Page<AsistenciaResponse>> listar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long estudianteId,
            @PageableDefault(size = 30) Pageable pageable) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_LECTURA);
        return ResponseEntity.ok(service.listar(estudianteId, pageable));
    }

    @GetMapping("/{asistenciaId}")
    @Operation(summary = "Obtener asistencia por id")
    public ResponseEntity<AsistenciaResponse> obtener(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long estudianteId,
            @PathVariable Long asistenciaId) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_LECTURA);
        return ResponseEntity.ok(service.obtener(estudianteId, asistenciaId));
    }

    @PostMapping
    @Operation(summary = "Registrar asistencia", description = "ADMIN, STAFF o INSTRUCTOR. Una por estudiante x asignacion.")
    public ResponseEntity<AsistenciaResponse> registrar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long estudianteId,
            @Valid @RequestBody CreateAsistenciaRequest request) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_REGISTRO);
        AsistenciaResponse creada = service.registrar(estudianteId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creada.id()).toUri();
        return ResponseEntity.created(location).body(creada);
    }

    @PutMapping("/{asistenciaId}")
    @Operation(summary = "Actualizar asistencia (correccion)")
    public ResponseEntity<AsistenciaResponse> actualizar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long estudianteId,
            @PathVariable Long asistenciaId,
            @Valid @RequestBody UpdateAsistenciaRequest request) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_REGISTRO);
        return ResponseEntity.ok(service.actualizar(estudianteId, asistenciaId, request));
    }

    @DeleteMapping("/{asistenciaId}")
    @Operation(summary = "Eliminar asistencia (hard delete)")
    public ResponseEntity<Void> eliminar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long estudianteId,
            @PathVariable Long asistenciaId) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, Set.of("ADMIN"));
        service.eliminar(estudianteId, asistenciaId);
        return ResponseEntity.noContent().build();
    }
}
