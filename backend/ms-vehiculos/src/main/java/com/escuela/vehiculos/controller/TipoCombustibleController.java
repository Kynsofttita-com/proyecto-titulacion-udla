package com.escuela.vehiculos.controller;

import com.escuela.common.security.headers.UserHeaders;
import com.escuela.vehiculos.dto.ActualizarPrecioRequest;
import com.escuela.vehiculos.dto.ActualizarTipoCombustibleRequest;
import com.escuela.vehiculos.dto.CrearTipoCombustibleRequest;
import com.escuela.vehiculos.dto.TipoCombustibleResponse;
import com.escuela.vehiculos.service.TipoCombustibleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * CRUD limitado para el catalogo de combustibles. Crear/eliminar no se exponen
 * (los tipos se gestionan por migracion Flyway). El admin solo actualiza el
 * precio vigente para reflejar variaciones del mercado.
 */
@RestController
@RequestMapping("/tipos-combustible")
@Tag(name = "Tipos de combustible", description = "Catalogo y configuracion de precios")
public class TipoCombustibleController {

    private static final Set<String> ROLES_LECTURA = Set.of("ADMIN", "STAFF", "INSTRUCTOR");
    private static final Set<String> ROLES_ESCRITURA = Set.of("ADMIN");

    private final TipoCombustibleService service;

    public TipoCombustibleController(TipoCombustibleService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar tipos de combustible (todos)")
    public ResponseEntity<List<TipoCombustibleResponse>> listar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @RequestParam(required = false, defaultValue = "false") boolean soloActivos) {
        validarAuth(userEmail, userRoles, ROLES_LECTURA);
        return ResponseEntity.ok(soloActivos ? service.listarActivos() : service.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un tipo de combustible")
    public ResponseEntity<TipoCombustibleResponse> obtener(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long id) {
        validarAuth(userEmail, userRoles, ROLES_LECTURA);
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo tipo de combustible (solo ADMIN)")
    public ResponseEntity<TipoCombustibleResponse> crear(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @Valid @RequestBody CrearTipoCombustibleRequest request) {
        validarAuth(userEmail, userRoles, ROLES_ESCRITURA);
        TipoCombustibleResponse creado = service.crear(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.id()).toUri();
        return ResponseEntity.created(location).body(creado);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desactivar un tipo de combustible (solo ADMIN, no se borra físico)")
    public ResponseEntity<Void> desactivar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long id) {
        validarAuth(userEmail, userRoles, ROLES_ESCRITURA);
        service.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar nombre y observaciones de un tipo (solo ADMIN). Codigo y unidad son inmutables.")
    public ResponseEntity<TipoCombustibleResponse> actualizar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long id,
            @Valid @RequestBody ActualizarTipoCombustibleRequest request) {
        validarAuth(userEmail, userRoles, ROLES_ESCRITURA);
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @PutMapping("/{id}/precio")
    @Operation(summary = "Actualizar el precio actual de un tipo (solo ADMIN)")
    public ResponseEntity<TipoCombustibleResponse> actualizarPrecio(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long id,
            @Valid @RequestBody ActualizarPrecioRequest request) {
        validarAuth(userEmail, userRoles, ROLES_ESCRITURA);
        return ResponseEntity.ok(service.actualizarPrecio(id, request));
    }

    private void validarAuth(String userEmail, String userRoles, Set<String> rolesPermitidos) {
        if (userEmail == null || userEmail.isBlank()) {
            throw new VehiculoController.NoAutenticadoException();
        }
        if (userRoles == null || userRoles.isBlank()) {
            throw new VehiculoController.SinPermisoException();
        }
        List<String> rolesUsuario = Arrays.asList(userRoles.split("\\s*,\\s*"));
        if (rolesUsuario.stream().noneMatch(rolesPermitidos::contains)) {
            throw new VehiculoController.SinPermisoException();
        }
    }
}
