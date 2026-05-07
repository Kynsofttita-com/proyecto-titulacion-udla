---
name: generate-rest-controller
description: Generate a Spring Boot REST controller with full CRUD endpoints, request/response DTOs, MapStruct mapper, service layer, OpenAPI annotations (SpringDoc), validation, error handling, and pagination. Use when adding a new REST API to a microservice.
---

# Generate REST Controller Skill

Creates a complete REST API stack: Controller + Service + DTOs + Mapper + OpenAPI docs.

## Inputs Needed

Ask the user for:
1. **Service**: which microservice
2. **Resource name**: PascalCase singular (e.g., `Estudiante`, `Vehiculo`)
3. **Base path**: kebab-case plural (e.g., `/v1/estudiantes`)
4. **Operations needed**: CRUD subset (list, get, create, update, delete) + custom
5. **Required roles per operation**: e.g., `ADMIN` only, `STAFF` for create
6. **Pagination**: yes (default) / no

## Generated Files

```
src/main/java/com/kynsoft/<context>/
├── interfaces/
│   ├── rest/<Resource>Controller.java
│   ├── dto/
│   │   ├── <Resource>Response.java
│   │   ├── Create<Resource>Request.java
│   │   └── Update<Resource>Request.java
│   └── mapper/<Resource>Mapper.java
├── application/service/<Resource>Service.java
└── domain/exception/
    ├── <Resource>NotFoundException.java
    └── Duplicate<Resource>Exception.java
```

## Templates

### Controller

```java
package com.kynsoft.<context>.interfaces.rest;

import com.kynsoft.<context>.application.service.<Resource>Service;
import com.kynsoft.<context>.interfaces.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Tag(name = "<Resource>", description = "Gestión de <resource_plural>")
@RestController
@RequestMapping("/v1/<resources>")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
public class <Resource>Controller {

    private final <Resource>Service service;

    @Operation(summary = "Listar <resources>", description = "Returns paginated list")
    @ApiResponse(responseCode = "200", description = "Page of <resources>")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PERSONAL_ADMINISTRATIVO')")
    public Page<<Resource>Response> list(@ParameterObject Pageable pageable) {
        return service.list(pageable);
    }

    @Operation(summary = "Obtener <resource> por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Found"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PERSONAL_ADMINISTRATIVO')")
    public <Resource>Response getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @Operation(summary = "Crear nuevo <resource>")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "409", description = "Conflict (duplicate)")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PERSONAL_ADMINISTRATIVO')")
    public ResponseEntity<<Resource>Response> create(
        @Valid @RequestBody Create<Resource>Request request
    ) {
        var response = service.create(request);
        var location = URI.create("/v1/<resources>/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Actualizar <resource>")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PERSONAL_ADMINISTRATIVO')")
    public <Resource>Response update(
        @PathVariable Long id,
        @Valid @RequestBody Update<Resource>Request request
    ) {
        return service.update(id, request);
    }

    @Operation(summary = "Eliminar <resource>")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
```

### DTOs (Java records)

```java
// Create<Resource>Request.java
public record Create<Resource>Request(
    @NotBlank @Pattern(regexp = "^[0-9]{10}$") String cedula,
    @NotBlank @Size(min = 2, max = 100) String nombres,
    @NotBlank @Size(min = 2, max = 100) String apellidos,
    @NotBlank @Email String email,
    @Pattern(regexp = "^0[0-9]{9}$") String telefono,
    @NotNull @Past LocalDate fechaNacimiento
) {}

// <Resource>Response.java
public record <Resource>Response(
    Long id,
    String cedula,
    String nombres,
    String apellidos,
    String email,
    String telefono,
    LocalDate fechaNacimiento,
    String estado,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {}
```

### MapStruct Mapper

```java
@Mapper(componentModel = "spring")
public interface <Resource>Mapper {
    
    <Resource>Response toResponse(<Resource> entity);
    
    <Resource> toEntity(Create<Resource>Request request);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(Update<Resource>Request request, @MappingTarget <Resource> entity);
}
```

### Service

```java
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class <Resource>Service {

    private final <Resource>Repository repository;
    private final <Resource>Mapper mapper;
    private final ApplicationEventPublisher eventPublisher;

    public Page<<Resource>Response> list(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toResponse);
    }

    public <Resource>Response getById(Long id) {
        var entity = repository.findById(id)
            .orElseThrow(() -> new <Resource>NotFoundException(id));
        return mapper.toResponse(entity);
    }

    @Transactional
    public <Resource>Response create(Create<Resource>Request request) {
        if (repository.existsByCedula(request.cedula())) {
            throw new Duplicate<Resource>Exception(request.cedula());
        }
        
        var entity = mapper.toEntity(request);
        entity = repository.save(entity);
        
        log.info("Created <Resource>: id={}, cedula={}", entity.getId(), entity.getCedula());
        eventPublisher.publishEvent(new <Resource>CreatedEvent(entity.getId()));
        
        return mapper.toResponse(entity);
    }

    @Transactional
    public <Resource>Response update(Long id, Update<Resource>Request request) {
        var entity = repository.findById(id)
            .orElseThrow(() -> new <Resource>NotFoundException(id));
        
        mapper.update(request, entity);
        entity = repository.save(entity);
        
        log.info("Updated <Resource>: id={}", id);
        return mapper.toResponse(entity);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new <Resource>NotFoundException(id);
        }
        repository.deleteById(id);
        log.info("Deleted <Resource>: id={}", id);
    }
}
```

### Domain Exceptions

```java
public class <Resource>NotFoundException extends RuntimeException {
    public <Resource>NotFoundException(Long id) {
        super("<Resource> con id " + id + " no encontrado");
    }
}

public class Duplicate<Resource>Exception extends RuntimeException {
    public Duplicate<Resource>Exception(String identifier) {
        super("Ya existe un <resource> con identificador: " + identifier);
    }
}
```

### Global Exception Handler (only if not yet exists)

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex) {
        var problem = ProblemDetail.forStatusAndDetail(BAD_REQUEST, "Validation failed");
        problem.setProperty("errors", ex.getBindingResult().getFieldErrors().stream()
            .map(err -> Map.of("field", err.getField(), "message", err.getDefaultMessage()))
            .toList());
        return ResponseEntity.status(BAD_REQUEST).body(problem);
    }

    @ExceptionHandler({<Resource>NotFoundException.class})
    public ResponseEntity<ProblemDetail> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(NOT_FOUND)
            .body(ProblemDetail.forStatusAndDetail(NOT_FOUND, ex.getMessage()));
    }

    @ExceptionHandler({Duplicate<Resource>Exception.class})
    public ResponseEntity<ProblemDetail> handleConflict(RuntimeException ex) {
        return ResponseEntity.status(CONFLICT)
            .body(ProblemDetail.forStatusAndDetail(CONFLICT, ex.getMessage()));
    }
}
```

## Workflow

1. **Read** existing controllers in the service to match conventions
2. **Generate** all 6 files (controller, 2 DTOs, mapper, service, exceptions)
3. **Update** existing GlobalExceptionHandler if it exists, or create one
4. **Run** `mvn compile` to verify
5. **Run** `mvn spring-boot:run` and test endpoints with curl/Postman/Swagger UI
6. **Update** OpenAPI spec file (`docs/api/<service>-openapi.yaml`)
7. **Suggest** writing tests with `generate-spring-test` skill

## Quality Checklist

- [ ] Controller annotated with @Tag, @RestController, @RequestMapping
- [ ] All endpoints documented with @Operation and @ApiResponse
- [ ] All endpoints have @PreAuthorize for security
- [ ] DTOs are Java records (immutable)
- [ ] Validation annotations on request DTOs
- [ ] Service is @Transactional appropriately (readOnly for queries)
- [ ] Logging at INFO for state changes
- [ ] MapStruct mapper generated
- [ ] Custom exceptions for domain errors
- [ ] Returns ResponseEntity with Location header on create
- [ ] Compiles without errors

## Notes

- Always use Java records for DTOs (immutability + concise)
- Use MapStruct for entity↔DTO conversion (compile-time safety)
- Pagination via Pageable (Spring Data)
- Versioning via URI: `/v1/`
- Security via `@PreAuthorize` (method-level)
- Log state changes (created, updated, deleted) at INFO
- Don't return entities — always DTOs
