package com.escuela.estudiantes.controller;

import com.escuela.common.security.headers.UserHeaders;
import com.escuela.estudiantes.dto.CreateEstudianteRequest;
import com.escuela.estudiantes.dto.EstudianteDetailResponse;
import com.escuela.estudiantes.dto.EstudianteListResponse;
import com.escuela.estudiantes.dto.EstudianteResponse;
import com.escuela.estudiantes.dto.UpdateEstudianteRequest;
import com.escuela.estudiantes.service.EstudianteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Controller REST de Estudiantes.
 *
 * <p>El API Gateway valida el JWT y agrega los headers {@code X-User-Email} y
 * {@code X-User-Roles}. Aqui confiamos en esos headers (no hay filtro JWT
 * propio en este MS). Si falta {@code X-User-Email} -&gt; 401. Si la operacion
 * requiere rol especifico y el usuario no lo tiene -&gt; 403.</p>
 */
@RestController
@RequestMapping("/estudiantes")
@Tag(name = "Estudiantes", description = "Gestion de estudiantes matriculados")
public class EstudianteController {

    private static final Set<String> ROLES_ESCRITURA = Set.of("ADMIN", "STAFF");
    private static final Set<String> ROLES_BORRADO = Set.of("ADMIN");

    private final EstudianteService service;

    public EstudianteController(EstudianteService service) {
        this.service = service;
    }

    // -----------------------------------------------------------------------
    // GET /estudiantes
    // -----------------------------------------------------------------------

    @GetMapping
    @Operation(summary = "Listar estudiantes",
            description = "Lista paginada con filtros opcionales por estado y busqueda en nombre/apellido/cedula/email.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pagina de estudiantes"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    public ResponseEntity<Page<EstudianteListResponse>> listar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @PageableDefault(size = 50, sort = "id") Pageable pageable,
            @Parameter(description = "Busqueda en nombre, apellido, cedula o email")
            @RequestParam(required = false) String search,
            @Parameter(description = "Filtrar por estado (PRE_MATRICULADO, ACTIVO, COMPLETADO, RETIRADO)")
            @RequestParam(required = false) String estado) {
        validarAutenticacion(userEmail);
        return ResponseEntity.ok(service.findAll(pageable, search, estado));
    }

    // -----------------------------------------------------------------------
    // GET /estudiantes/{id}
    // -----------------------------------------------------------------------

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de un estudiante",
            description = "Retorna el estudiante con sus documentos y contactos de emergencia.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estudiante encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    public ResponseEntity<EstudianteDetailResponse> obtener(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @PathVariable Long id) {
        validarAutenticacion(userEmail);
        return ResponseEntity.ok(service.findById(id));
    }

    // -----------------------------------------------------------------------
    // POST /estudiantes
    // -----------------------------------------------------------------------

    @PostMapping
    @Operation(summary = "Crear un estudiante",
            description = "Solo accesible para roles ADMIN o STAFF.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Creado, devuelve Location header"),
            @ApiResponse(responseCode = "400", description = "Validacion fallida (cedula EC, email, telefono)"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos (rol)"),
            @ApiResponse(responseCode = "409", description = "Cedula o email ya existen")
    })
    public ResponseEntity<EstudianteResponse> crear(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @Valid @RequestBody CreateEstudianteRequest request) {
        validarAutenticacion(userEmail);
        validarRoles(userRoles, ROLES_ESCRITURA);

        EstudianteResponse creado = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creado.id())
                .toUri();
        return ResponseEntity.created(location).body(creado);
    }

    // -----------------------------------------------------------------------
    // PUT /estudiantes/{id}
    // -----------------------------------------------------------------------

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un estudiante",
            description = "PATCH semantics: solo los campos enviados se actualizan. Solo ADMIN o STAFF.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Actualizado"),
            @ApiResponse(responseCode = "400", description = "Validacion fallida"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos"),
            @ApiResponse(responseCode = "404", description = "No encontrado"),
            @ApiResponse(responseCode = "409", description = "Email duplicado")
    })
    public ResponseEntity<EstudianteResponse> actualizar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long id,
            @Valid @RequestBody UpdateEstudianteRequest request) {
        validarAutenticacion(userEmail);
        validarRoles(userRoles, ROLES_ESCRITURA);
        return ResponseEntity.ok(service.update(id, request));
    }

    // -----------------------------------------------------------------------
    // DELETE /estudiantes/{id}
    // -----------------------------------------------------------------------

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un estudiante (soft delete)",
            description = "Solo accesible para ADMIN. Marca deletedAt = now().")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Eliminado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos (solo ADMIN)"),
            @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    public ResponseEntity<Void> eliminar(
            @RequestHeader(value = UserHeaders.USER_EMAIL, required = false) String userEmail,
            @RequestHeader(value = UserHeaders.USER_ROLES, required = false) String userRoles,
            @PathVariable Long id) {
        validarAutenticacion(userEmail);
        validarRoles(userRoles, ROLES_BORRADO);
        service.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    // -----------------------------------------------------------------------
    // Helpers de seguridad (manual, basado en headers del Gateway)
    // -----------------------------------------------------------------------

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
        boolean autorizado = rolesUsuario.stream().anyMatch(rolesPermitidos::contains);
        if (!autorizado) {
            throw new SinPermisoException();
        }
    }

    // Excepciones internas del controller (mapeadas a 401/403 por el handler)
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
