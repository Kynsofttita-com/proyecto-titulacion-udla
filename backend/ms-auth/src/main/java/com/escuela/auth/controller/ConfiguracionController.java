package com.escuela.auth.controller;

import com.escuela.auth.dto.ConfiguracionResponse;
import com.escuela.auth.dto.UpdateConfiguracionRequest;
import com.escuela.auth.security.AuthHeaderGuard;
import com.escuela.auth.service.ConfiguracionService;
import com.escuela.common.security.headers.UserHeaders;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/configuracion")
@Tag(name = "Configuracion", description = "Configuracion de la escuela (single-tenant)")
public class ConfiguracionController {

    private static final Set<String> ROLES_LECTURA = Set.of("ADMIN", "STAFF");
    private static final Set<String> ROLES_ESCRITURA = Set.of("ADMIN");

    private final ConfiguracionService service;

    public ConfiguracionController(ConfiguracionService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Obtener configuracion de la escuela")
    public ResponseEntity<ConfiguracionResponse> obtener(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_LECTURA);
        return ResponseEntity.ok(service.obtener());
    }

    @PutMapping
    @Operation(summary = "Actualizar configuracion de la escuela", description = "Solo ADMIN")
    public ResponseEntity<ConfiguracionResponse> actualizar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @Valid @RequestBody UpdateConfiguracionRequest request) {
        AuthHeaderGuard.requireAuth(userEmail);
        AuthHeaderGuard.requireAnyRole(userRoles, ROLES_ESCRITURA);
        return ResponseEntity.ok(service.actualizar(request));
    }
}
