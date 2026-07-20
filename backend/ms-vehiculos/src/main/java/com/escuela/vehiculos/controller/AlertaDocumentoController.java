package com.escuela.vehiculos.controller;

import com.escuela.common.security.headers.UserHeaders;
import com.escuela.vehiculos.dto.AlertaDocumentoResponse;
import com.escuela.vehiculos.security.AuthHeaderGuard;
import com.escuela.vehiculos.service.AlertaSoatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

/**
 * Alertas unificadas de vencimientos de documentos del vehiculo (SOAT + RTV).
 * Reemplaza al endpoint /vehiculos/alertas-soat cuando la UI quiere mostrar
 * ambos tipos en un mismo widget/panel.
 */
@RestController
@RequestMapping("/vehiculos/alertas-documentos")
@Tag(name = "Vehiculos - Alertas Documentos", description = "SOAT + RTV vencidos o por vencer")
public class AlertaDocumentoController {

    private static final Set<String> ROLES_LECTURA = Set.of("ADMIN", "STAFF");

    private final AlertaSoatService service;

    public AlertaDocumentoController(AlertaSoatService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Vehiculos con SOAT o RTV vencido o por vencer",
            description = "Por defecto 30 dias. Un vehiculo puede aparecer 2 veces si ambos documentos estan por vencer.")
    public ResponseEntity<List<AlertaDocumentoResponse>> alertas(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @RequestParam(defaultValue = "30") int dias) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_LECTURA);
        if (dias < 0 || dias > 365) {
            throw new IllegalArgumentException("dias debe estar entre 0 y 365");
        }
        return ResponseEntity.ok(service.alertasDocumentos(dias));
    }
}
