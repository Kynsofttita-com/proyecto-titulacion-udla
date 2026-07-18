package com.escuela.notificaciones.controller;

import com.escuela.notificaciones.dto.NotificacionResponse;
import com.escuela.notificaciones.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService service;

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'INSTRUCTOR', 'ESTUDIANTE')")
    public ResponseEntity<NotificacionResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'INSTRUCTOR', 'ESTUDIANTE')")
    public ResponseEntity<Page<NotificacionResponse>> listarPorUsuario(
        @RequestParam Long usuarioId,
        Pageable pageable
    ) {
        return ResponseEntity.ok(service.listarPorUsuario(usuarioId, pageable));
    }

    @GetMapping("/filtrar")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'INSTRUCTOR', 'ESTUDIANTE')")
    public ResponseEntity<Page<NotificacionResponse>> listarConFiltros(
        @RequestParam Long usuarioId,
        @RequestParam(required = false) Boolean leida,
        @RequestParam(required = false) String tipo,
        @RequestParam(required = false) String prioridad,
        Pageable pageable
    ) {
        return ResponseEntity.ok(service.listarPorUsuarioConFiltros(usuarioId, leida, tipo, prioridad, pageable));
    }

    @PutMapping("/{id}/marcar-leida")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'INSTRUCTOR', 'ESTUDIANTE')")
    public ResponseEntity<NotificacionResponse> marcarComoLeida(@PathVariable Long id) {
        return ResponseEntity.ok(service.marcarComoLeida(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'INSTRUCTOR', 'ESTUDIANTE')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/usuario/{usuarioId}/marcar-todas-leidas")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'INSTRUCTOR', 'ESTUDIANTE')")
    public ResponseEntity<java.util.Map<String, Integer>> marcarTodasComoLeidas(@PathVariable Long usuarioId) {
        int afectadas = service.marcarTodasComoLeidas(usuarioId);
        return ResponseEntity.ok(java.util.Map.of("actualizadas", afectadas));
    }

    @DeleteMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'INSTRUCTOR', 'ESTUDIANTE')")
    public ResponseEntity<java.util.Map<String, Integer>> eliminarTodasPorUsuario(@PathVariable Long usuarioId) {
        int afectadas = service.eliminarTodasPorUsuario(usuarioId);
        return ResponseEntity.ok(java.util.Map.of("eliminadas", afectadas));
    }
}
