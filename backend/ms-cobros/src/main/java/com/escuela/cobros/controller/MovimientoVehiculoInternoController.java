package com.escuela.cobros.controller;

import com.escuela.cobros.dto.MovimientoVehiculoRequest;
import com.escuela.cobros.entity.MovimientoContable;
import com.escuela.cobros.service.MovimientoContableService;
import com.escuela.common.security.headers.UserHeaders;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Endpoints INTERNOS invocados por ms-vehiculos (via Feign) para sincronizar
 * gastos de combustible y mantenimiento con contabilidad.
 *
 * <p>NO expuestos por el api-gateway al frontend; solo se llegan por Feign
 * MS-a-MS. Requieren rol SYSTEM o ADMIN (rol SYSTEM lo pone el FeignConfig
 * cuando la llamada viene de un job de background sin request context).</p>
 */
@RestController
@RequestMapping("/internal/movimientos-vehiculo")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Interno - Movimientos desde vehiculos",
     description = "Sincronizacion contable de combustible y mantenimiento (solo Feign MS-a-MS)")
public class MovimientoVehiculoInternoController {

    private final MovimientoContableService service;

    // ---------------- COMBUSTIBLE ----------------

    @PostMapping("/combustible/{registroId}")
    @Operation(summary = "Crea el movimiento GASTO asociado a un registro de combustible")
    public ResponseEntity<Void> crearCombustible(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long registroId,
            @Valid @RequestBody MovimientoVehiculoRequest request) {
        validarAuth(userEmail, userRoles);
        MovimientoContable m = service.crearDesdeCombustible(registroId, request);
        // 204 si se saltea (no hay cuenta default); 201 si se creo.
        return m == null
                ? ResponseEntity.noContent().build()
                : ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/combustible/{registroId}")
    @Operation(summary = "Sincroniza el movimiento cuando cambia el registro de combustible")
    public ResponseEntity<Void> actualizarCombustible(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long registroId,
            @Valid @RequestBody MovimientoVehiculoRequest request) {
        validarAuth(userEmail, userRoles);
        service.actualizarDesdeCombustible(registroId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/combustible/{registroId}")
    @Operation(summary = "Anula el movimiento cuando se elimina el registro de combustible")
    public ResponseEntity<Void> anularCombustible(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long registroId,
            @RequestParam(required = false) String motivo) {
        validarAuth(userEmail, userRoles);
        service.anularDesdeCombustible(registroId, motivo);
        return ResponseEntity.noContent().build();
    }

    // ---------------- MANTENIMIENTO ----------------

    @PostMapping("/mantenimiento/{mantenimientoId}")
    @Operation(summary = "Crea el movimiento GASTO asociado a un mantenimiento")
    public ResponseEntity<Void> crearMantenimiento(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long mantenimientoId,
            @Valid @RequestBody MovimientoVehiculoRequest request) {
        validarAuth(userEmail, userRoles);
        MovimientoContable m = service.crearDesdeMantenimiento(mantenimientoId, request);
        return m == null
                ? ResponseEntity.noContent().build()
                : ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/mantenimiento/{mantenimientoId}")
    @Operation(summary = "Sincroniza el movimiento cuando cambia el mantenimiento")
    public ResponseEntity<Void> actualizarMantenimiento(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long mantenimientoId,
            @Valid @RequestBody MovimientoVehiculoRequest request) {
        validarAuth(userEmail, userRoles);
        service.actualizarDesdeMantenimiento(mantenimientoId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/mantenimiento/{mantenimientoId}")
    @Operation(summary = "Anula el movimiento cuando se elimina el mantenimiento")
    public ResponseEntity<Void> anularMantenimiento(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long mantenimientoId,
            @RequestParam(required = false) String motivo) {
        validarAuth(userEmail, userRoles);
        service.anularDesdeMantenimiento(mantenimientoId, motivo);
        return ResponseEntity.noContent().build();
    }

    // ---------------- helpers ----------------

    /**
     * Solo aceptamos calls desde otros MS (rol SYSTEM que inyecta el
     * FeignConfig) o desde un ADMIN autenticado (util para debug/curl).
     */
    private void validarAuth(String userEmail, String userRoles) {
        if (userEmail == null || userEmail.isBlank() || userRoles == null || userRoles.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Endpoint interno: requiere headers de identidad");
        }
        if (!userRoles.contains("SYSTEM") && !userRoles.contains("ADMIN")) {
            log.warn("Intento de acceso a endpoint interno por {} con roles {}", userEmail, userRoles);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Endpoint interno: solo SYSTEM/ADMIN");
        }
    }
}
