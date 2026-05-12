package com.escuela.vehiculos.controller;

import com.escuela.common.security.headers.UserHeaders;
import com.escuela.vehiculos.dto.CreateVehiculoRequest;
import com.escuela.vehiculos.dto.UpdateVehiculoRequest;
import com.escuela.vehiculos.dto.VehiculoListResponse;
import com.escuela.vehiculos.dto.VehiculoResponse;
import com.escuela.vehiculos.service.VehiculoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/vehiculos")
@Tag(name = "Vehículos", description = "Gestión de flota vehicular")
public class VehiculoController {

    private static final Set<String> ROLES_ESCRITURA = Set.of("ADMIN", "STAFF");
    private static final Set<String> ROLES_BORRADO = Set.of("ADMIN");

    private final VehiculoService service;

    public VehiculoController(VehiculoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar vehículos")
    public ResponseEntity<Page<VehiculoListResponse>> listar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @PageableDefault(size = 50, sort = "id") Pageable pageable) {
        validarAutenticacion(userEmail);
        return ResponseEntity.ok(service.findAll(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de un vehículo")
    public ResponseEntity<VehiculoResponse> obtener(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @PathVariable Long id) {
        validarAutenticacion(userEmail);
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    @Operation(summary = "Registrar un vehículo")
    public ResponseEntity<VehiculoResponse> crear(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @Valid @RequestBody CreateVehiculoRequest request) {
        validarAutenticacion(userEmail);
        validarRoles(userRoles, ROLES_ESCRITURA);

        VehiculoResponse creado = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creado.id())
                .toUri();
        return ResponseEntity.created(location).body(creado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un vehículo")
    public ResponseEntity<VehiculoResponse> actualizar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long id,
            @Valid @RequestBody UpdateVehiculoRequest request) {
        validarAutenticacion(userEmail);
        validarRoles(userRoles, ROLES_ESCRITURA);
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un vehículo (soft delete)")
    public ResponseEntity<Void> eliminar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long id) {
        validarAutenticacion(userEmail);
        validarRoles(userRoles, ROLES_BORRADO);
        service.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    private void validarAutenticacion(String userEmail) {
        if (userEmail == null || userEmail.isBlank()) {
            throw new NoAutenticadoException();
        }
    }

    private void validarRoles(String userRolesHeader, Set<String> rolesPermitidos) {
        if (userRolesHeader == null || userRolesHeader.isBlank()) {
            throw new SinPermisoException();
        }
        List<String> rolesUsuario = Arrays.asList(userRolesHeader.split("\\s*,\\s*"));
        if (!rolesUsuario.stream().anyMatch(rolesPermitidos::contains)) {
            throw new SinPermisoException();
        }
    }

    public static class NoAutenticadoException extends RuntimeException {
        public NoAutenticadoException() {
            super("Token de autenticacion requerido");
        }
    }

    public static class SinPermisoException extends RuntimeException {
        public SinPermisoException() {
            super("El rol del usuario no permite esta operacion");
        }
    }
}
