package com.escuela.instructores.controller;

import com.escuela.common.security.headers.UserHeaders;
import com.escuela.instructores.dto.CreateInstructorRequest;
import com.escuela.instructores.dto.DisponibilidadDelDiaResponse;
import com.escuela.instructores.dto.InstructorListResponse;
import com.escuela.instructores.dto.InstructorResponse;
import com.escuela.instructores.dto.ResumenHorasResponse;
import com.escuela.instructores.dto.UpdateInstructorRequest;
import com.escuela.instructores.security.AuthHeaderGuard;
import com.escuela.instructores.service.DisponibilidadService;
import com.escuela.instructores.service.InstructorService;
import com.escuela.instructores.service.ResumenHorasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.Set;

@RestController
@RequestMapping("/instructores")
@Tag(name = "Instructores", description = "Gestion de instructores de la escuela")
public class InstructorController {

    private static final Set<String> ROLES_LECTURA = Set.of("ADMIN", "STAFF", "INSTRUCTOR");
    private static final Set<String> ROLES_ESCRITURA = Set.of("ADMIN", "STAFF");
    private static final Set<String> ROLES_BORRADO = Set.of("ADMIN");

    private final InstructorService service;
    private final DisponibilidadService disponibilidadService;
    private final ResumenHorasService resumenHorasService;

    public InstructorController(InstructorService service,
                                DisponibilidadService disponibilidadService,
                                ResumenHorasService resumenHorasService) {
        this.service = service;
        this.disponibilidadService = disponibilidadService;
        this.resumenHorasService = resumenHorasService;
    }

    @GetMapping
    @Operation(summary = "Listar instructores con filtros opcionales")
    public ResponseEntity<Page<InstructorListResponse>> listar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PageableDefault(size = 50, sort = "id") Pageable pageable,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String estado) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_LECTURA);
        return ResponseEntity.ok(service.listar(pageable, search, estado));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener instructor por id")
    public ResponseEntity<InstructorResponse> obtener(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long id) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_LECTURA);
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    @Operation(summary = "Crear instructor", description = "ADMIN o STAFF")
    public ResponseEntity<InstructorResponse> crear(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @Valid @RequestBody CreateInstructorRequest request) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_ESCRITURA);
        InstructorResponse creado = service.crear(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.id()).toUri();
        return ResponseEntity.created(location).body(creado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar instructor", description = "ADMIN o STAFF")
    public ResponseEntity<InstructorResponse> actualizar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long id,
            @Valid @RequestBody UpdateInstructorRequest request) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_ESCRITURA);
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar instructor (soft delete)", description = "Solo ADMIN")
    public ResponseEntity<Void> eliminar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long id) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_BORRADO);
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint clave para MS-Asignaciones (Sprint 6): determina si el
     * instructor tiene franjas disponibles en una fecha especifica.
     */
    @GetMapping("/{id}/disponibilidad")
    @Operation(summary = "Calcular disponibilidad del dia (recurrente + excepciones)")
    public ResponseEntity<DisponibilidadDelDiaResponse> disponibilidadDelDia(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_LECTURA);
        return ResponseEntity.ok(disponibilidadService.disponibilidadDelDia(id, fecha));
    }

    @GetMapping("/{id}/resumen-horas")
    @Operation(summary = "Resumen de horas trabajadas y sueldo estimado en un rango")
    public ResponseEntity<ResumenHorasResponse> resumenHoras(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_LECTURA);
        return ResponseEntity.ok(resumenHorasService.calcular(id, desde, hasta));
    }
}
