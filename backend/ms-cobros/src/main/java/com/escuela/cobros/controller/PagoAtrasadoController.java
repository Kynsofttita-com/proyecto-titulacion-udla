package com.escuela.cobros.controller;

import com.escuela.cobros.service.PagoAtrasadoScheduler;
import com.escuela.common.security.headers.UserHeaders;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/facturas/atrasos")
@Tag(name = "Cobros - Atrasos", description = "Scheduler manual de facturas atrasadas")
@RequiredArgsConstructor
public class PagoAtrasadoController {

    private final PagoAtrasadoScheduler scheduler;

    @PostMapping("/publicar")
    @Operation(summary = "Dispara manualmente el scheduler de facturas atrasadas",
            description = "En prod corre 08:10 diariamente. Uso: dev/testing.")
    public ResponseEntity<String> disparar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles) {
        if (userEmail == null || userEmail.isBlank()) {
            return ResponseEntity.status(401).body("No autenticado");
        }
        if (userRoles == null || !userRoles.contains("ADMIN")) {
            return ResponseEntity.status(403).body("Solo ADMIN puede disparar");
        }
        scheduler.publicarPagosAtrasados();
        return ResponseEntity.accepted().body("Publicacion iniciada");
    }
}
