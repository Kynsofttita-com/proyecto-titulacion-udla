package com.escuela.cobros.controller;

import com.escuela.cobros.dto.EstadoCuentaResponse;
import com.escuela.cobros.service.EstadoCuentaService;
import com.escuela.common.security.headers.UserHeaders;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/estado-cuenta")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Estado de Cuenta", description = "Consulta del estado de cuenta de estudiantes")
public class EstadoCuentaController {

    private final EstadoCuentaService estadoCuentaService;
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_STAFF = "STAFF";

    @GetMapping("/estudiante/{estudianteId}")
    @Operation(summary = "Obtener estado de cuenta del estudiante")
    public ResponseEntity<EstadoCuentaResponse> obtenerEstadoCuenta(
        @PathVariable Long estudianteId,
        @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
        @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles
    ) {
        validarAutenticacion(userEmail);

        log.debug("Obteniendo estado de cuenta para estudiante: {} solicitado por: {}", estudianteId, userEmail);
        EstadoCuentaResponse estadoCuenta = estadoCuentaService.obtenerEstadoCuenta(estudianteId);
        return ResponseEntity.ok(estadoCuenta);
    }

    private void validarAutenticacion(String userEmail) {
        if (userEmail == null || userEmail.isBlank()) {
            log.warn("Usuario no autenticado, intento de acceso a estado de cuenta");
            throw new RuntimeException("Usuario no autenticado");
        }
    }
}
