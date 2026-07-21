package com.escuela.cobros.controller;

import com.escuela.cobros.dto.CategoriaMovimientoRequest;
import com.escuela.cobros.dto.CategoriaMovimientoResponse;
import com.escuela.cobros.service.CategoriaMovimientoService;
import com.escuela.common.security.headers.UserHeaders;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/categorias-movimiento")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Categorias de movimiento", description = "Categorias de ingresos y gastos")
public class CategoriaMovimientoController {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_STAFF = "STAFF";

    private final CategoriaMovimientoService service;

    @GetMapping
    @Operation(summary = "Listar categorias (opcionalmente filtradas por tipo INGRESO/GASTO)")
    public ResponseEntity<List<CategoriaMovimientoResponse>> listar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false, defaultValue = "true") boolean soloActivas) {
        validarAuth(userEmail, userRoles, ROLE_ADMIN, ROLE_STAFF);
        if (soloActivas) {
            return ResponseEntity.ok(service.listarActivas(tipo));
        }
        return ResponseEntity.ok(service.listarTodas());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una categoria")
    public ResponseEntity<CategoriaMovimientoResponse> obtener(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long id) {
        validarAuth(userEmail, userRoles, ROLE_ADMIN, ROLE_STAFF);
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    @Operation(summary = "Crear una categoria custom (solo ADMIN)")
    public ResponseEntity<CategoriaMovimientoResponse> crear(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @Valid @RequestBody CategoriaMovimientoRequest request) {
        validarAuth(userEmail, userRoles, ROLE_ADMIN);
        CategoriaMovimientoResponse creada = service.crear(request);
        return ResponseEntity.created(
                ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}").buildAndExpand(creada.id()).toUri()
        ).body(creada);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar categoria (solo ADMIN). Las de sistema solo permiten cambiar nombre.")
    public ResponseEntity<CategoriaMovimientoResponse> actualizar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long id,
            @Valid @RequestBody CategoriaMovimientoRequest request) {
        validarAuth(userEmail, userRoles, ROLE_ADMIN);
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desactivar o eliminar categoria (solo ADMIN). Las de sistema no se pueden borrar.")
    public ResponseEntity<Void> desactivar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long id) {
        validarAuth(userEmail, userRoles, ROLE_ADMIN);
        service.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    private void validarAuth(String userEmail, String userRoles, String... rolesPermitidos) {
        if (userEmail == null || userEmail.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado");
        }
        if (userRoles == null || userRoles.isBlank()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario sin roles");
        }
        boolean tiene = false;
        for (String rol : rolesPermitidos) {
            if (userRoles.contains(rol)) { tiene = true; break; }
        }
        if (!tiene) {
            log.warn("Usuario {} sin permisos: {}", userEmail, userRoles);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sin permisos suficientes");
        }
    }
}
