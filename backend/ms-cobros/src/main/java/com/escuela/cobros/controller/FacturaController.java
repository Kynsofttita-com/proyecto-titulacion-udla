package com.escuela.cobros.controller;

import com.escuela.cobros.dto.FacturaCuotaResponse;
import com.escuela.cobros.dto.FacturaListResponse;
import com.escuela.cobros.dto.FacturaRequest;
import com.escuela.cobros.dto.FacturaResponse;
import com.escuela.cobros.service.FacturaService;
import com.escuela.cobros.service.SituacionPagoCalculator;
import java.util.List;
import java.util.Map;
import com.escuela.common.security.headers.UserHeaders;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/facturas")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Facturas", description = "Gestión de facturas y facturación")
public class FacturaController {

    private final FacturaService facturaService;
    private final SituacionPagoCalculator situacionPagoCalculator;
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_STAFF = "STAFF";

    @GetMapping
    @Operation(summary = "Listar todas las facturas")
    public ResponseEntity<Page<FacturaListResponse>> listar(
        @PageableDefault(size = 50) Pageable pageable
    ) {
        log.debug("Listando facturas con paginación: {}", pageable);
        Page<FacturaListResponse> facturas = facturaService.findAll(pageable);
        return ResponseEntity.ok(facturas);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener factura por ID")
    public ResponseEntity<FacturaResponse> obtener(@PathVariable Long id) {
        log.debug("Obteniendo factura con ID: {}", id);
        FacturaResponse factura = facturaService.findById(id);
        return ResponseEntity.ok(factura);
    }

    @GetMapping("/estudiante/{estudianteId}")
    @Operation(summary = "Listar facturas de un estudiante")
    public ResponseEntity<Page<FacturaListResponse>> listarPorEstudiante(
        @PathVariable Long estudianteId,
        @PageableDefault(size = 50) Pageable pageable
    ) {
        log.debug("Listando facturas del estudiante: {}", estudianteId);
        Page<FacturaListResponse> facturas = facturaService.findByEstudianteId(estudianteId, pageable);
        return ResponseEntity.ok(facturas);
    }

    @GetMapping("/{id}/cuotas")
    @Operation(summary = "Listar cuotas de una factura (vacío si CONTADO)")
    public ResponseEntity<List<FacturaCuotaResponse>> listarCuotas(@PathVariable Long id) {
        log.debug("Listando cuotas de factura: {}", id);
        return ResponseEntity.ok(facturaService.findCuotas(id));
    }

    @GetMapping("/estudiante/{estudianteId}/situacion-pago")
    @Operation(summary = "Calcula la situacion_pago actual del estudiante",
               description = "Lee facturas y cuotas en vivo. Usado por MS-Estudiantes para sincronizar drifts.")
    public ResponseEntity<Map<String, String>> calcularSituacionPago(@PathVariable Long estudianteId) {
        log.debug("Calculando situacion_pago para estudiante: {}", estudianteId);
        String situacion = situacionPagoCalculator.calcular(estudianteId);
        return ResponseEntity.ok(Map.of("situacionPago", situacion));
    }

    @PostMapping
    @Operation(summary = "Crear nueva factura")
    public ResponseEntity<FacturaResponse> crear(
        @Valid @RequestBody FacturaRequest request,
        @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
        @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles
    ) {
        validarAutenticacion(userEmail);
        validarPermisos(userRoles, ROLE_ADMIN, ROLE_STAFF);

        log.info("Creando factura para estudiante: {} por usuario: {}", request.estudianteId(), userEmail);
        FacturaResponse factura = facturaService.create(request);

        return ResponseEntity.created(
            ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(factura.id())
                .toUri()
        ).body(factura);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar factura")
    public ResponseEntity<FacturaResponse> actualizar(
        @PathVariable Long id,
        @Valid @RequestBody FacturaRequest request,
        @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
        @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles
    ) {
        validarAutenticacion(userEmail);
        validarPermisos(userRoles, ROLE_ADMIN, ROLE_STAFF);

        log.info("Actualizando factura: {} por usuario: {}", id, userEmail);
        FacturaResponse factura = facturaService.update(id, request);
        return ResponseEntity.ok(factura);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar (soft-delete) factura")
    public ResponseEntity<Void> eliminar(
        @PathVariable Long id,
        @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
        @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles
    ) {
        validarAutenticacion(userEmail);
        validarPermisos(userRoles, ROLE_ADMIN);

        log.info("Eliminando factura: {} por usuario: {}", id, userEmail);
        facturaService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    private void validarAutenticacion(String userEmail) {
        if (userEmail == null || userEmail.isBlank()) {
            log.warn("Usuario no autenticado, intento de acceso a facturación");
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
            log.warn("Usuario sin permisos necesarios para operación en facturación: {}", userRoles);
            throw new RuntimeException("Usuario sin permisos");
        }
    }
}
