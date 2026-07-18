package com.escuela.instructores.controller;

import com.escuela.common.security.headers.UserHeaders;
import com.escuela.instructores.security.AuthHeaderGuard;
import com.escuela.instructores.service.LicenciaAlertaScheduler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/instructores/alertas-licencia")
@Tag(name = "Instructores - Alertas Licencia", description = "Scheduler de vencimiento de licencias")
public class LicenciaAlertaController {

    private final LicenciaAlertaScheduler scheduler;

    public LicenciaAlertaController(LicenciaAlertaScheduler scheduler) {
        this.scheduler = scheduler;
    }

    @PostMapping("/publicar")
    @Operation(summary = "Dispara manualmente el scheduler de alertas de licencia",
            description = "Solo ADMIN. Uso: dev/testing. En prod corre 08:05 diario.")
    public ResponseEntity<String> disparar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, Set.of("ADMIN"));
        scheduler.publicarAlertasLicencia();
        return ResponseEntity.accepted().body("Publicacion iniciada");
    }
}
