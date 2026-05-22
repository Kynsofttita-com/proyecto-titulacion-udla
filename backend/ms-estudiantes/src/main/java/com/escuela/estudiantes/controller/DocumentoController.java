package com.escuela.estudiantes.controller;

import com.escuela.common.security.headers.UserHeaders;
import com.escuela.estudiantes.dto.CreateDocumentoRequest;
import com.escuela.estudiantes.dto.DocumentoResponse;
import com.escuela.estudiantes.security.AuthHeaderGuard;
import com.escuela.estudiantes.service.DocumentoService;
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
@RequestMapping("/estudiantes/{estudianteId}/documentos")
@Tag(name = "Estudiantes - Documentos", description = "Documentos subidos por el estudiante")
public class DocumentoController {

    private static final Set<String> ROLES_LECTURA = Set.of("ADMIN", "STAFF", "INSTRUCTOR", "ESTUDIANTE");
    private static final Set<String> ROLES_ESCRITURA = Set.of("ADMIN", "STAFF");

    private final DocumentoService service;

    public DocumentoController(DocumentoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar documentos del estudiante")
    public ResponseEntity<List<DocumentoResponse>> listar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long estudianteId) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_LECTURA);
        return ResponseEntity.ok(service.listarPorEstudiante(estudianteId));
    }

    @GetMapping("/{documentoId}")
    @Operation(summary = "Obtener documento por id")
    public ResponseEntity<DocumentoResponse> obtener(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long estudianteId,
            @PathVariable Long documentoId) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_LECTURA);
        return ResponseEntity.ok(service.obtener(estudianteId, documentoId));
    }

    @PostMapping
    @Operation(summary = "Subir documento", description = "URL del archivo en MinIO. ADMIN/STAFF.")
    public ResponseEntity<DocumentoResponse> subir(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long estudianteId,
            @Valid @RequestBody CreateDocumentoRequest request) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_ESCRITURA);
        DocumentoResponse creado = service.subir(estudianteId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.id()).toUri();
        return ResponseEntity.created(location).body(creado);
    }

    @DeleteMapping("/{documentoId}")
    @Operation(summary = "Eliminar documento (soft delete). ADMIN/STAFF.")
    public ResponseEntity<Void> eliminar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long estudianteId,
            @PathVariable Long documentoId) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_ESCRITURA);
        service.eliminar(estudianteId, documentoId);
        return ResponseEntity.noContent().build();
    }
}
