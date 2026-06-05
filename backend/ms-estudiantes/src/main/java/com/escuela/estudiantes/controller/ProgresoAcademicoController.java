package com.escuela.estudiantes.controller;

import com.escuela.common.security.headers.UserHeaders;
import com.escuela.estudiantes.dto.ProgresoAcademicoHorasResponse;
import com.escuela.estudiantes.dto.ProgresoAcademicoResponse;
import com.escuela.estudiantes.dto.UpdateProgresoAcademicoRequest;
import com.escuela.estudiantes.security.AuthHeaderGuard;
import com.escuela.estudiantes.service.ProgresoAcademicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/estudiantes/{estudianteId}/progreso")
@Tag(name = "Estudiantes - Progreso Academico", description = "Progreso academico del estudiante")
public class ProgresoAcademicoController {

    private static final Set<String> ROLES_LECTURA = Set.of("ADMIN", "STAFF", "INSTRUCTOR", "ESTUDIANTE");
    private static final Set<String> ROLES_ESCRITURA = Set.of("ADMIN", "STAFF");

    private final ProgresoAcademicoService service;

    public ProgresoAcademicoController(ProgresoAcademicoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Obtener progreso academico del estudiante")
    public ResponseEntity<ProgresoAcademicoResponse> obtener(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long estudianteId) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_LECTURA);
        return ResponseEntity.ok(service.obtener(estudianteId));
    }

    @GetMapping("/horas")
    @Operation(summary = "Obtener progreso en horas (para vista detallada)")
    public ResponseEntity<ProgresoAcademicoHorasResponse> obtenerHoras(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long estudianteId) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_LECTURA);
        return ResponseEntity.ok(service.obtenerHoras(estudianteId));
    }

    @PutMapping
    @Operation(summary = "Actualizar/crear progreso", description = "Upsert. Solo ADMIN o STAFF.")
    public ResponseEntity<ProgresoAcademicoResponse> actualizar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long estudianteId,
            @Valid @RequestBody UpdateProgresoAcademicoRequest request) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_ESCRITURA);
        return ResponseEntity.ok(service.actualizar(estudianteId, request));
    }
}
