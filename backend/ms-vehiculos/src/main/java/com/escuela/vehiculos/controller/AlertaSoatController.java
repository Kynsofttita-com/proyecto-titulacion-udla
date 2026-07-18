package com.escuela.vehiculos.controller;

import com.escuela.common.security.headers.UserHeaders;
import com.escuela.vehiculos.dto.AlertaSoatResponse;
import com.escuela.vehiculos.security.AuthHeaderGuard;
import com.escuela.vehiculos.service.AlertaSoatService;
import com.escuela.vehiculos.service.SoatAlertaScheduler;
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
    private final SoatAlertaScheduler scheduler;

    public AlertaSoatController(AlertaSoatService service, SoatAlertaScheduler scheduler) {
        this.service = service;
        this.scheduler = scheduler;
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

    @PostMapping("/publicar")
    @Operation(summary = "Dispara manualmente el scheduler de publicacion de alertas SOAT",
            description = "Solo ADMIN. Uso: dev/testing. En prod corre 08:00 diario.")
    public ResponseEntity<String> disparar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, Set.of("ADMIN"));
        scheduler.publicarAlertasSoat();
        return ResponseEntity.accepted().body("Publicacion iniciada");
    }
}
