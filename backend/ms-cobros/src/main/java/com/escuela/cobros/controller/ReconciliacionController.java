package com.escuela.cobros.controller;

import com.escuela.cobros.dto.ReconciliacionListResponse;
import com.escuela.cobros.dto.ReconciliacionRequest;
import com.escuela.cobros.dto.ReconciliacionResponse;
import com.escuela.cobros.service.ReconciliacionService;
import com.escuela.common.security.headers.UserHeaders;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/reconciliaciones")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reconciliaciones", description = "Gestión de reconciliaciones diarias de pagos")
public class ReconciliacionController {

    private final ReconciliacionService reconciliacionService;
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_STAFF = "STAFF";

    @GetMapping
    @Operation(summary = "Listar todas las reconciliaciones")
    public ResponseEntity<Page<ReconciliacionListResponse>> listar(
        @PageableDefault(size = 50) Pageable pageable
    ) {
        log.debug("Listando reconciliaciones con paginación: {}", pageable);
        Page<ReconciliacionListResponse> reconciliaciones = reconciliacionService.findAll(pageable);
        return ResponseEntity.ok(reconciliaciones);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener reconciliación por ID")
    public ResponseEntity<ReconciliacionResponse> obtener(@PathVariable Long id) {
        log.debug("Obteniendo reconciliación con ID: {}", id);
        ReconciliacionResponse reconciliacion = reconciliacionService.findById(id);
        return ResponseEntity.ok(reconciliacion);
    }

    @GetMapping("/fecha/{fecha}")
    @Operation(summary = "Obtener reconciliación por fecha")
    public ResponseEntity<ReconciliacionResponse> obtenerPorFecha(@PathVariable String fecha) {
        log.debug("Obteniendo reconciliación para fecha: {}", fecha);
        LocalDate localDate = LocalDate.parse(fecha, DateTimeFormatter.ISO_LOCAL_DATE);
        ReconciliacionResponse reconciliacion = reconciliacionService.findByFecha(localDate)
            .orElseThrow(() -> new RuntimeException("Reconciliación para la fecha " + fecha + " no encontrada"));
        return ResponseEntity.ok(reconciliacion);
    }

    @PostMapping
    @Operation(summary = "Crear nueva reconciliación")
    public ResponseEntity<ReconciliacionResponse> crear(
        @Valid @RequestBody ReconciliacionRequest request,
        @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
        @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles
    ) {
        validarAutenticacion(userEmail);
        validarPermisos(userRoles, ROLE_ADMIN, ROLE_STAFF);

        log.info("Creando reconciliación para fecha: {} por usuario: {}", request.fecha(), userEmail);
        ReconciliacionResponse reconciliacion = reconciliacionService.create(request);

        return ResponseEntity.created(
            ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(reconciliacion.id())
                .toUri()
        ).body(reconciliacion);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar reconciliación")
    public ResponseEntity<ReconciliacionResponse> actualizar(
        @PathVariable Long id,
        @Valid @RequestBody ReconciliacionRequest request,
        @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
        @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles
    ) {
        validarAutenticacion(userEmail);
        validarPermisos(userRoles, ROLE_ADMIN, ROLE_STAFF);

        log.info("Actualizando reconciliación: {} por usuario: {}", id, userEmail);
        ReconciliacionResponse reconciliacion = reconciliacionService.update(id, request);
        return ResponseEntity.ok(reconciliacion);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar reconciliación")
    public ResponseEntity<Void> eliminar(
        @PathVariable Long id,
        @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
        @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles
    ) {
        validarAutenticacion(userEmail);
        validarPermisos(userRoles, ROLE_ADMIN);

        log.info("Eliminando reconciliación: {} por usuario: {}", id, userEmail);
        reconciliacionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private void validarAutenticacion(String userEmail) {
        if (userEmail == null || userEmail.isBlank()) {
            log.warn("Usuario no autenticado, intento de acceso a reconciliaciones");
            throw new RuntimeException("Usuario no autenticado");
        }
    }

    private void validarPermisos(String userRoles, String... rolesPermitidos) {
        if (userRoles == null || userRoles.isBlank()) {
            log.warn("Usuario sin roles, acceso denegado");
            throw new RuntimeException("Usuario sin roles");
        }

        boolean tienePermiso = false;
        for (String rol : rolesPermitidos) {
            if (userRoles.contains(rol)) {
                tienePermiso = true;
                break;
            }
        }

        if (!tienePermiso) {
            log.warn("Usuario sin permisos necesarios para operación en reconciliaciones: {}", userRoles);
            throw new RuntimeException("Usuario sin permisos");
        }
    }
}
