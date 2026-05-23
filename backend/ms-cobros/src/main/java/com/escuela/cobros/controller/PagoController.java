package com.escuela.cobros.controller;

import com.escuela.cobros.dto.PagoListResponse;
import com.escuela.cobros.dto.PagoRequest;
import com.escuela.cobros.dto.PagoResponse;
import com.escuela.cobros.service.PagoService;
import com.escuela.common.security.headers.UserHeaders;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/pagos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Pagos", description = "Gestión de pagos y registros de cobro")
public class PagoController {

    private final PagoService pagoService;
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_STAFF = "STAFF";

    @GetMapping
    @Operation(summary = "Listar todos los pagos")
    public ResponseEntity<Page<PagoListResponse>> listar(
        @PageableDefault(size = 50) Pageable pageable
    ) {
        log.debug("Listando pagos con paginación: {}", pageable);
        Page<PagoListResponse> pagos = pagoService.findAll(pageable);
        return ResponseEntity.ok(pagos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener pago por ID")
    public ResponseEntity<PagoResponse> obtener(@PathVariable Long id) {
        log.debug("Obteniendo pago con ID: {}", id);
        PagoResponse pago = pagoService.findById(id);
        return ResponseEntity.ok(pago);
    }

    @GetMapping("/factura/{facturaId}")
    @Operation(summary = "Listar pagos de una factura")
    public ResponseEntity<Page<PagoListResponse>> listarPorFactura(
        @PathVariable Long facturaId,
        @PageableDefault(size = 50) Pageable pageable
    ) {
        log.debug("Listando pagos de la factura: {}", facturaId);
        Page<PagoListResponse> pagos = pagoService.findByFacturaId(facturaId, pageable);
        return ResponseEntity.ok(pagos);
    }

    @GetMapping("/estudiante/{estudianteId}")
    @Operation(summary = "Listar pagos de un estudiante")
    public ResponseEntity<Page<PagoListResponse>> listarPorEstudiante(
        @PathVariable Long estudianteId,
        @PageableDefault(size = 50) Pageable pageable
    ) {
        log.debug("Listando pagos del estudiante: {}", estudianteId);
        Page<PagoListResponse> pagos = pagoService.findByEstudianteId(estudianteId, pageable);
        return ResponseEntity.ok(pagos);
    }

    @PostMapping
    @Operation(summary = "Registrar nuevo pago")
    public ResponseEntity<PagoResponse> registrar(
        @Valid @RequestBody PagoRequest request,
        @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
        @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles
    ) {
        validarAutenticacion(userEmail);
        validarPermisos(userRoles, ROLE_ADMIN, ROLE_STAFF);

        log.info("Registrando pago para factura: {} por usuario: {}", request.facturaId(), userEmail);
        PagoResponse pago = pagoService.create(request);

        return ResponseEntity.created(
            ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(pago.id())
                .toUri()
        ).body(pago);
    }

    private void validarAutenticacion(String userEmail) {
        if (userEmail == null || userEmail.isBlank()) {
            log.warn("Usuario no autenticado, intento de acceso a pagos");
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
            log.warn("Usuario sin permisos necesarios para operación en pagos: {}", userRoles);
            throw new RuntimeException("Usuario sin permisos");
        }
    }
}
