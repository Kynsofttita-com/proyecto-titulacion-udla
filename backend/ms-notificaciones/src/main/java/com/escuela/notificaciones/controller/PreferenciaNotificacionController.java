package com.escuela.notificaciones.controller;

import com.escuela.notificaciones.dto.PreferenciaResponse;
import com.escuela.notificaciones.dto.UpdatePreferenciaRequest;
import com.escuela.notificaciones.service.PreferenciaNotificacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/preferencias")
@RequiredArgsConstructor
public class PreferenciaNotificacionController {

    private final PreferenciaNotificacionService service;

    @GetMapping("/{usuarioId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'INSTRUCTOR', 'ESTUDIANTE')")
    public ResponseEntity<PreferenciaResponse> obtenerPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(service.obtenerPorUsuario(usuarioId));
    }

    @PutMapping("/{usuarioId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'INSTRUCTOR', 'ESTUDIANTE')")
    public ResponseEntity<PreferenciaResponse> actualizar(
        @PathVariable Long usuarioId,
        @Valid @RequestBody UpdatePreferenciaRequest request
    ) {
        return ResponseEntity.ok(service.actualizar(usuarioId, request));
    }

    @PostMapping("/{usuarioId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<PreferenciaResponse> crearOActualizar(
        @PathVariable Long usuarioId,
        @Valid @RequestBody UpdatePreferenciaRequest request
    ) {
        return ResponseEntity.ok(service.crearOActualizar(usuarioId, request));
    }
}
