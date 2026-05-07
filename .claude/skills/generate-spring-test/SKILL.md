---
name: generate-spring-test
description: Generate JUnit 5 unit and integration tests for Spring Boot components (services, controllers, repositories) with Mockito, AssertJ, Testcontainers, and proper test data builders. Targets 80%+ coverage following the project's testing standards.
---

# Generate Spring Test Skill

Creates comprehensive tests for Spring Boot components.

## Inputs Needed

Ask the user for:
1. **Test type**: unit, slice (@WebMvcTest/@DataJpaTest), or integration (@SpringBootTest)
2. **Class to test**: full class name (e.g., `StudentService`, `StudentController`)
3. **Coverage focus**: happy path, edge cases, error handling, all

## Test Templates

### 1. Service Unit Test (Mockito)

```java
package com.kynsoft.<context>.application.service;

import com.kynsoft.<context>.domain.exception.*;
import com.kynsoft.<context>.infrastructure.persistence.entity.*;
import com.kynsoft.<context>.infrastructure.persistence.repository.*;
import com.kynsoft.<context>.interfaces.dto.*;
import com.kynsoft.<context>.interfaces.mapper.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("<Resource>Service Unit Tests")
class <Resource>ServiceTest {

    @Mock
    private <Resource>Repository repository;

    @Mock
    private <Resource>Mapper mapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private <Resource>Service service;

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("should create <resource> when request is valid")
        void should_create_<resource>_when_validRequest() {
            // Arrange
            var request = <Resource>TestDataBuilder.validCreateRequest().build();
            var entity = <Resource>TestDataBuilder.validEntity().build();
            var response = <Resource>TestDataBuilder.validResponse().build();

            when(repository.existsByCedula(request.cedula())).thenReturn(false);
            when(mapper.toEntity(request)).thenReturn(entity);
            when(repository.save(entity)).thenReturn(entity);
            when(mapper.toResponse(entity)).thenReturn(response);

            // Act
            var result = service.create(request);

            // Assert
            assertThat(result).isEqualTo(response);
            verify(repository).save(entity);
            verify(eventPublisher).publishEvent(any(<Resource>CreatedEvent.class));
        }

        @Test
        @DisplayName("should throw exception when cedula already exists")
        void should_throwException_when_cedulaExists() {
            // Arrange
            var request = <Resource>TestDataBuilder.validCreateRequest().build();
            when(repository.existsByCedula(request.cedula())).thenReturn(true);

            // Act + Assert
            assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(Duplicate<Resource>Exception.class)
                .hasMessageContaining(request.cedula());

            verify(repository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }
    }

    @Nested
    @DisplayName("getById()")
    class GetById {

        @Test
        @DisplayName("should return <resource> when found")
        void should_returnResource_when_found() {
            // Arrange
            var id = 1L;
            var entity = <Resource>TestDataBuilder.validEntity().id(id).build();
            var response = <Resource>TestDataBuilder.validResponse().id(id).build();

            when(repository.findById(id)).thenReturn(Optional.of(entity));
            when(mapper.toResponse(entity)).thenReturn(response);

            // Act
            var result = service.getById(id);

            // Assert
            assertThat(result).isEqualTo(response);
        }

        @Test
        @DisplayName("should throw NotFoundException when id does not exist")
        void should_throwNotFound_when_idDoesNotExist() {
            // Arrange
            var id = 999L;
            when(repository.findById(id)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> service.getById(id))
                .isInstanceOf(<Resource>NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("update()")
    class Update {
        // similar pattern
    }

    @Nested
    @DisplayName("delete()")
    class Delete {
        // similar pattern
    }
}
```

### 2. Test Data Builder

```java
package com.kynsoft.<context>.test;

import com.kynsoft.<context>.infrastructure.persistence.entity.*;
import com.kynsoft.<context>.interfaces.dto.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public class <Resource>TestDataBuilder {

    public static EntityBuilder validEntity() {
        return new EntityBuilder();
    }

    public static CreateRequestBuilder validCreateRequest() {
        return new CreateRequestBuilder();
    }

    public static ResponseBuilder validResponse() {
        return new ResponseBuilder();
    }

    public static class EntityBuilder {
        private Long id = 1L;
        private String cedula = "1712345678";
        private String nombres = "Juan Carlos";
        private String apellidos = "Pérez González";
        private String email = "juan@example.com";
        private LocalDate fechaNacimiento = LocalDate.of(2000, 1, 15);

        public EntityBuilder id(Long id) { this.id = id; return this; }
        public EntityBuilder cedula(String cedula) { this.cedula = cedula; return this; }
        // ... other setters

        public <Resource> build() {
            return <Resource>.builder()
                .id(id)
                .cedula(cedula)
                .nombres(nombres)
                .apellidos(apellidos)
                .email(email)
                .fechaNacimiento(fechaNacimiento)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .createdBy("test")
                .updatedBy("test")
                .version(0L)
                .build();
        }
    }

    public static class CreateRequestBuilder {
        private String cedula = "1712345678";
        // ... fields

        public Create<Resource>Request build() {
            return new Create<Resource>Request(
                cedula, nombres, apellidos, email, telefono, fechaNacimiento
            );
        }
    }

    public static class ResponseBuilder { /* similar */ }
}
```

### 3. Controller Slice Test (@WebMvcTest)

```java
package com.kynsoft.<context>.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kynsoft.<context>.application.service.<Resource>Service;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(<Resource>Controller.class)
@DisplayName("<Resource>Controller Tests")
class <Resource>ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private <Resource>Service service;

    @Test
    @DisplayName("POST /v1/<resources> should return 201 when valid")
    @WithMockUser(roles = "ADMIN")
    void should_return201_when_validRequest() throws Exception {
        var request = <Resource>TestDataBuilder.validCreateRequest().build();
        var response = <Resource>TestDataBuilder.validResponse().build();
        
        when(service.create(any())).thenReturn(response);

        mockMvc.perform(post("/v1/<resources>")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.id").value(response.id()))
            .andExpect(jsonPath("$.cedula").value(response.cedula()));
    }

    @Test
    @DisplayName("POST /v1/<resources> should return 400 when invalid cedula")
    @WithMockUser(roles = "ADMIN")
    void should_return400_when_invalidCedula() throws Exception {
        var invalidRequest = """
            {"cedula":"123","nombres":"Juan","apellidos":"Pérez",
             "email":"juan@example.com","fechaNacimiento":"2000-01-01"}
            """;

        mockMvc.perform(post("/v1/<resources>")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors[?(@.field=='cedula')]").exists());
    }

    @Test
    @DisplayName("POST /v1/<resources> should return 401 when not authenticated")
    void should_return401_when_notAuthenticated() throws Exception {
        var request = <Resource>TestDataBuilder.validCreateRequest().build();

        mockMvc.perform(post("/v1/<resources>")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /v1/<resources> should return 403 when wrong role")
    @WithMockUser(roles = "ESTUDIANTE")
    void should_return403_when_wrongRole() throws Exception {
        var request = <Resource>TestDataBuilder.validCreateRequest().build();

        mockMvc.perform(post("/v1/<resources>")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }
}
```

### 4. Repository Slice Test (@DataJpaTest)

```java
package com.kynsoft.<context>.infrastructure.persistence.repository;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class <Resource>RepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private <Resource>Repository repository;

    @Test
    @DisplayName("findByCedula should return entity when exists")
    void findByCedula_when_exists() {
        // Arrange
        var entity = <Resource>TestDataBuilder.validEntity().build();
        em.persistAndFlush(entity);

        // Act
        var result = repository.findByCedula(entity.getCedula());

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(entity.getId());
    }

    @Test
    @DisplayName("existsByCedula should return false when not exists")
    void existsByCedula_when_notExists() {
        var result = repository.existsByCedula("0000000000");
        assertThat(result).isFalse();
    }
}
```

### 5. Integration Test (@SpringBootTest + Testcontainers)

```java
package com.kynsoft.<context>;

import com.kynsoft.<context>.interfaces.dto.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@DisplayName("<Resource> Integration Tests")
class <Resource>IntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
        .withDatabaseName("test")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Full enrollment flow")
    void fullFlow_create_get_update_delete() throws Exception {
        // Create
        var createResponse = mockMvc.perform(post("/v1/<resources>")
            .contentType("application/json")
            .content("""
                {"cedula":"1712345678","nombres":"Juan",...}
                """))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        
        Long id = JsonPath.read(createResponse, "$.id");

        // Get
        mockMvc.perform(get("/v1/<resources>/" + id))
            .andExpect(status().isOk());

        // Update, Delete...
    }
}
```

## Workflow

1. **Read** the class to test
2. **Identify** all public methods + their branches
3. **Generate** test data builder (reusable)
4. **Generate** unit tests for service (Mockito)
5. **Generate** slice tests for controller (MockMvc) and repository (DataJpaTest)
6. **Generate** integration test for happy path (Testcontainers)
7. **Run** `mvn verify` and check coverage
8. **Add** tests for any uncovered branches
9. **Report** coverage achieved

## Quality Checklist

- [ ] Test class has @DisplayName matching the class name
- [ ] Tests use @Nested for grouping by method
- [ ] Test names follow `should_<expectedBehavior>_when_<condition>`
- [ ] AAA pattern (Arrange, Act, Assert) clearly visible
- [ ] AssertJ used for fluent assertions
- [ ] Test data builders used (no duplicated literals)
- [ ] Both happy and error paths covered
- [ ] @WithMockUser for security tests
- [ ] Coverage target met (80%+)
- [ ] No flaky tests (deterministic, no Thread.sleep)
- [ ] Tests run fast (unit < 100ms each)

## Common Patterns

```java
// Verify never called
verify(eventPublisher, never()).publishEvent(any());

// Verify called once
verify(repository, times(1)).save(any());

// Verify with argument captor
var captor = ArgumentCaptor.forClass(<Resource>.class);
verify(repository).save(captor.capture());
assertThat(captor.getValue().getCedula()).isEqualTo("1712345678");

// Mock chained calls
when(repo.findById(1L)).thenReturn(Optional.of(entity));
when(mapper.toResponse(entity)).thenReturn(response);
```

## Notes

- Use `@MockBean` only in `@SpringBootTest`; use `@Mock` + `@InjectMocks` for unit tests
- Slice tests are FAST (only load relevant context)
- Integration tests are SLOW (full Spring context + DB) — use sparingly
- Always test edge cases: null, empty, boundary values, invalid input
- Don't test framework code (don't test Spring's @Transactional itself)
