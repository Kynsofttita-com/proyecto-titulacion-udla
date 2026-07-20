package com.escuela.asignaciones.controller;

import com.escuela.common.security.headers.UserHeaders;
import com.escuela.asignaciones.dto.CreateAsignacionRequest;
import com.escuela.asignaciones.dto.FinalizarAsignacionRequest;
import com.escuela.asignaciones.dto.HorasCumplidasResponse;
import com.escuela.asignaciones.dto.IniciarAsignacionRequest;
import com.escuela.asignaciones.dto.RecorridoResponse;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
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

    // ========================================================================
    //  KILOMETRAJE: iniciar / finalizar / recorrido
    //  Roles ampliados: INSTRUCTOR puede registrar km de SUS clases en la practica.
    //  (Si en el futuro queremos restringir a "su" instructor, se valida en service)
    // ========================================================================

    private static final Set<String> ROLES_KILOMETRAJE = Set.of("ADMIN", "STAFF", "INSTRUCTOR");

    @PatchMapping("/{id}/iniciar")
    @Operation(summary = "Iniciar una clase: registra km_inicial y marca EN_CURSO")
    public ResponseEntity<RecorridoResponse> iniciar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long id,
            @Valid @RequestBody IniciarAsignacionRequest request) {
        validarAutenticacion(userEmail);
        validarRoles(userRoles, ROLES_KILOMETRAJE);
        return ResponseEntity.ok(service.iniciar(id, request));
    }

    @PatchMapping("/{id}/finalizar")
    @Operation(summary = "Finalizar una clase: registra km_final, marca COMPLETADA y sync con ms-vehiculos")
    public ResponseEntity<RecorridoResponse> finalizar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long id,
            @Valid @RequestBody FinalizarAsignacionRequest request) {
        validarAutenticacion(userEmail);
        validarRoles(userRoles, ROLES_KILOMETRAJE);
        return ResponseEntity.ok(service.finalizar(id, request));
    }

    @GetMapping("/{id}/recorrido")
    @Operation(summary = "Resumen del recorrido (km, duración real, etc.)")
    public ResponseEntity<RecorridoResponse> obtenerRecorrido(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @PathVariable Long id) {
        validarAutenticacion(userEmail);
        return ResponseEntity.ok(service.obtenerRecorrido(id));
    }

    @GetMapping("/instructor/{instructorId}/horas-cumplidas")
    @Operation(summary = "Horas de clases COMPLETADA por instructor en un rango",
            description = "Suma la duracion real (o programada si no hay real) de todas las clases COMPLETADA del instructor entre desde y hasta (inclusive).")
    public ResponseEntity<HorasCumplidasResponse> horasCumplidasInstructor(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @PathVariable Long instructorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        validarAutenticacion(userEmail);
        return ResponseEntity.ok(service.horasCumplidasInstructor(instructorId, desde, hasta));
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
