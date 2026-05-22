package com.escuela.vehiculos.controller;

import com.escuela.common.security.headers.UserHeaders;
import com.escuela.vehiculos.dto.AlertaSoatResponse;
import com.escuela.vehiculos.security.AuthHeaderGuard;
import com.escuela.vehiculos.service.AlertaSoatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/vehiculos/alertas-soat")
@Tag(name = "Vehiculos - Alertas SOAT", description = "Alertas de SOAT vencido o por vencer")
public class AlertaSoatController {

    private static final Set<String> ROLES_LECTURA = Set.of("ADMIN", "STAFF");

    private final AlertaSoatService service;

    public AlertaSoatController(AlertaSoatService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Vehiculos con SOAT vencido o por vencer",
            description = "Por defecto 30 dias. Incluye los ya vencidos (diasParaVencer < 0).")
    public ResponseEntity<List<AlertaSoatResponse>> alertas(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @RequestParam(defaultValue = "30") int dias) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_LECTURA);
        if (dias < 0 || dias > 365) {
            throw new IllegalArgumentException("dias debe estar entre 0 y 365");
        }
        return ResponseEntity.ok(service.alertasSoat(dias));
    }
}
