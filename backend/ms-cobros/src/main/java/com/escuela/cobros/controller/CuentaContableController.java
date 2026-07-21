package com.escuela.cobros.controller;

import com.escuela.cobros.dto.CuentaRequest;
import com.escuela.cobros.dto.CuentaResponse;
import com.escuela.cobros.service.CuentaContableService;
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
@RequestMapping("/cuentas")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cuentas contables", description = "Cuentas donde ingresan o salen los movimientos (caja, bancos, tarjetas)")
public class CuentaContableController {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_STAFF = "STAFF";

    private final CuentaContableService service;

    @GetMapping
    @Operation(summary = "Listar cuentas (todas o solo activas)")
    public ResponseEntity<List<CuentaResponse>> listar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @RequestParam(required = false, defaultValue = "false") boolean soloActivas) {
        validarAuth(userEmail, userRoles, ROLE_ADMIN, ROLE_STAFF);
        return ResponseEntity.ok(soloActivas ? service.listarActivas() : service.listarTodas());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una cuenta con su saldo actual")
    public ResponseEntity<CuentaResponse> obtener(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long id) {
        validarAuth(userEmail, userRoles, ROLE_ADMIN, ROLE_STAFF);
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    @Operation(summary = "Crear una nueva cuenta (solo ADMIN)")
    public ResponseEntity<CuentaResponse> crear(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @Valid @RequestBody CuentaRequest request) {
        validarAuth(userEmail, userRoles, ROLE_ADMIN);
        CuentaResponse creada = service.crear(request);
        return ResponseEntity.created(
                ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}").buildAndExpand(creada.id()).toUri()
        ).body(creada);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una cuenta (solo ADMIN)")
    public ResponseEntity<CuentaResponse> actualizar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long id,
            @Valid @RequestBody CuentaRequest request) {
        validarAuth(userEmail, userRoles, ROLE_ADMIN);
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desactivar una cuenta (solo ADMIN)")
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
