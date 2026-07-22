package com.escuela.cobros.controller;

import com.escuela.cobros.dto.AnularMovimientoRequest;
import com.escuela.cobros.dto.MovimientoContableRequest;
import com.escuela.cobros.dto.MovimientoContableResponse;
import com.escuela.cobros.service.MovimientoContableService;
import com.escuela.common.security.headers.UserHeaders;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDate;

@RestController
@RequestMapping("/movimientos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Movimientos contables", description = "Registro de ingresos y gastos")
public class MovimientoContableController {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_STAFF = "STAFF";

    private final MovimientoContableService service;

    @GetMapping
    @Operation(summary = "Listar movimientos con filtros opcionales")
    public ResponseEntity<Page<MovimientoContableResponse>> buscar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @RequestParam(required = false) Long cuentaId,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) Long vehiculoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @PageableDefault(size = 50) Pageable pageable) {
        validarAuth(userEmail, userRoles, ROLE_ADMIN, ROLE_STAFF);
        return ResponseEntity.ok(service.buscar(cuentaId, categoriaId, tipo, vehiculoId, fechaInicio, fechaFin, pageable));
    }

    @GetMapping("/resumen-vehiculo/{vehiculoId}")
    @Operation(summary = "Resumen de gastos (combustible + mantenimiento) de un vehiculo")
    public ResponseEntity<java.util.Map<String, java.math.BigDecimal>> resumenVehiculo(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long vehiculoId) {
        validarAuth(userEmail, userRoles, ROLE_ADMIN, ROLE_STAFF);
        return ResponseEntity.ok(service.resumenGastosVehiculo(vehiculoId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un movimiento")
    public ResponseEntity<MovimientoContableResponse> obtener(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long id) {
        validarAuth(userEmail, userRoles, ROLE_ADMIN, ROLE_STAFF);
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    @Operation(summary = "Crear un movimiento manual (gasto o ingreso)")
    public ResponseEntity<MovimientoContableResponse> crear(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @Valid @RequestBody MovimientoContableRequest request) {
        validarAuth(userEmail, userRoles, ROLE_ADMIN, ROLE_STAFF);
        MovimientoContableResponse creado = service.crear(request);
        return ResponseEntity.created(
                ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}").buildAndExpand(creado.id()).toUri()
        ).body(creado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un movimiento manual (no aplica a auto-generados desde pago)")
    public ResponseEntity<MovimientoContableResponse> actualizar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long id,
            @Valid @RequestBody MovimientoContableRequest request) {
        validarAuth(userEmail, userRoles, ROLE_ADMIN, ROLE_STAFF);
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @PostMapping("/{id}/anular")
    @Operation(summary = "Anular un movimiento manual (los originados por pago no se pueden anular)")
    public ResponseEntity<Void> anular(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long id,
            @Valid @RequestBody AnularMovimientoRequest request) {
        validarAuth(userEmail, userRoles, ROLE_ADMIN);
        service.anular(id, request);
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
