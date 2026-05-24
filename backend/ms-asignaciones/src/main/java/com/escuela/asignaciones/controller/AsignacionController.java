package com.escuela.asignaciones.controller;

import com.escuela.common.security.headers.UserHeaders;
import com.escuela.asignaciones.dto.CreateAsignacionRequest;
import com.escuela.asignaciones.dto.UpdateAsignacionRequest;
import com.escuela.asignaciones.dto.UpdateAsignacionReprogramarRequest;
import com.escuela.asignaciones.dto.AsignacionListResponse;
import com.escuela.asignaciones.dto.AsignacionResponse;
import com.escuela.asignaciones.service.AsignacionService;
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
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/asignaciones")
@Tag(name = "Asignaciones", description = "Gestión de asignaciones de clases")
public class AsignacionController {

    private static final Set<String> ROLES_ESCRITURA = Set.of("ADMIN", "STAFF");
    private static final Set<String> ROLES_BORRADO = Set.of("ADMIN");

    private final AsignacionService service;

    public AsignacionController(AsignacionService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar asignaciones")
    public ResponseEntity<Page<AsignacionListResponse>> listar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @PageableDefault(size = 50, sort = "id") Pageable pageable) {
        validarAutenticacion(userEmail);
        return ResponseEntity.ok(service.findAll(pageable));
    }

    @GetMapping("/estudiante/{estudianteId}")
    @Operation(summary = "Listar asignaciones de un estudiante (historial)")
    public ResponseEntity<Page<AsignacionListResponse>> listarPorEstudiante(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @PathVariable Long estudianteId,
            @PageableDefault(size = 100, sort = "fechaHora") Pageable pageable) {
        validarAutenticacion(userEmail);
        return ResponseEntity.ok(service.findByEstudianteId(estudianteId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de una asignación")
    public ResponseEntity<AsignacionResponse> obtener(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @PathVariable Long id) {
        validarAutenticacion(userEmail);
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    @Operation(summary = "Crear una asignación")
    public ResponseEntity<AsignacionResponse> crear(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @Valid @RequestBody CreateAsignacionRequest request) {
        validarAutenticacion(userEmail);
        validarRoles(userRoles, ROLES_ESCRITURA);

        AsignacionResponse creada = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creada.id())
                .toUri();
        return ResponseEntity.created(location).body(creada);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una asignación")
    public ResponseEntity<AsignacionResponse> actualizar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long id,
            @Valid @RequestBody UpdateAsignacionRequest request) {
        validarAutenticacion(userEmail);
        validarRoles(userRoles, ROLES_ESCRITURA);
        return ResponseEntity.ok(service.update(id, request));
    }

    @PutMapping("/{id}/reprogramar")
    @Operation(summary = "Reprogramar una asignación")
    public ResponseEntity<AsignacionResponse> reprogramar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long id,
            @Valid @RequestBody UpdateAsignacionReprogramarRequest request) {
        validarAutenticacion(userEmail);
        validarRoles(userRoles, ROLES_ESCRITURA);
        return ResponseEntity.ok(service.reprogramar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una asignación (soft delete)")
    public ResponseEntity<Void> eliminar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long id) {
        validarAutenticacion(userEmail);
        validarRoles(userRoles, ROLES_BORRADO);
        service.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    private void validarAutenticacion(String userEmail) {
        if (userEmail == null || userEmail.isBlank()) {
            throw new NoAutenticadoException();
        }
    }

    private void validarRoles(String userRolesHeader, Set<String> rolesPermitidos) {
        if (userRolesHeader == null || userRolesHeader.isBlank()) {
            throw new SinPermisoException();
        }
        List<String> rolesUsuario = Arrays.asList(userRolesHeader.split("\\s*,\\s*"));
        if (!rolesUsuario.stream().anyMatch(rolesPermitidos::contains)) {
            throw new SinPermisoException();
        }
    }

    public static class NoAutenticadoException extends RuntimeException {
        public NoAutenticadoException() {
            super("Token de autenticacion requerido");
        }
    }

    public static class SinPermisoException extends RuntimeException {
        public SinPermisoException() {
            super("El rol del usuario no permite esta operacion");
        }
    }
}
