package com.escuela.estudiantes.controller;

import com.escuela.estudiantes.dto.EstudianteDetailResponse;
import com.escuela.estudiantes.dto.EstudianteResponse;
import com.escuela.estudiantes.exception.CedulaDuplicadaException;
import com.escuela.estudiantes.exception.CedulaInvalidaException;
import com.escuela.estudiantes.exception.EmailDuplicadoException;
import com.escuela.estudiantes.exception.EstudianteNotFoundException;
import com.escuela.estudiantes.service.EstudianteEstadoService;
import com.escuela.estudiantes.service.EstudianteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EstudianteController.class)
@Import(EstudianteControllerTest.MockedServiceConfig.class)
@DisplayName("EstudianteController")
class EstudianteControllerTest {

    private static final String ADMIN_EMAIL = "admin@escuela.local";
    private static final String ADMIN_ROLES = "ADMIN";
    private static final String STAFF_ROLES = "STAFF";
    private static final String STUDENT_ROLES = "ESTUDIANTE";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private EstudianteService service;

    @TestConfiguration
    static class MockedServiceConfig {
        @Bean
        EstudianteService estudianteService() {
            return mock(EstudianteService.class);
        }

        @Bean
        EstudianteEstadoService estudianteEstadoService() {
            return mock(EstudianteEstadoService.class);
        }
    }

    @BeforeEach
    void resetMocks() {
        reset(service);
    }

    // -------------------------------------------------------------------
    // GET /estudiantes
    // -------------------------------------------------------------------

    @Test
    @DisplayName("GET list sin X-User-Email -> 401")
    void listSinAutenticacion() throws Exception {
        mockMvc.perform(get("/estudiantes"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.type").value("https://escuela.local/errors/missing-token"));
    }

    @Test
    @DisplayName("GET list autenticado -> 200")
    void listAutenticado() throws Exception {
        when(service.findAll(any(Pageable.class), any(), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/estudiantes")
                        .header("X-User-Email", ADMIN_EMAIL))
                .andExpect(status().isOk());
    }

    // -------------------------------------------------------------------
    // GET /estudiantes/{id}
    // -------------------------------------------------------------------

    @Test
    @DisplayName("GET /{id} existente -> 200")
    void getByIdOk() throws Exception {
        when(service.findById(1L)).thenReturn(new EstudianteDetailResponse(
                1L, "1710034065", "Hernan", "Jurado", "h@t.com", "0987654321",
                null, null, "M", "PRE_MATRICULADO", null, null, null, null, null,
                null, 0, null, null, null, null));

        mockMvc.perform(get("/estudiantes/1")
                        .header("X-User-Email", ADMIN_EMAIL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.cedula").value("1710034065"));
    }

    @Test
    @DisplayName("GET /{id} inexistente -> 404 Problem Details")
    void getByIdNotFound() throws Exception {
        when(service.findById(999L)).thenThrow(new EstudianteNotFoundException(999L));

        mockMvc.perform(get("/estudiantes/999")
                        .header("X-User-Email", ADMIN_EMAIL))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("https://escuela.local/errors/estudiante-not-found"))
                .andExpect(jsonPath("$.status").value(404));
    }

    // -------------------------------------------------------------------
    // POST /estudiantes
    // -------------------------------------------------------------------

    @Test
    @DisplayName("POST sin roles -> 403")
    void postSinRoles() throws Exception {
        mockMvc.perform(post("/estudiantes")
                        .header("X-User-Email", ADMIN_EMAIL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateBody()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.type").value("https://escuela.local/errors/forbidden"));
    }

    @Test
    @DisplayName("POST con rol ESTUDIANTE -> 403")
    void postRolInsuficiente() throws Exception {
        mockMvc.perform(post("/estudiantes")
                        .header("X-User-Email", ADMIN_EMAIL)
                        .header("X-User-Roles", STUDENT_ROLES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateBody()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST con rol STAFF y body valido -> 201 + Location")
    void postOkStaff() throws Exception {
        when(service.create(any())).thenReturn(new EstudianteResponse(
                7L, "1710034065", "Hernan", "Jurado", "h@t.com", "0987654321",
                "PRE_MATRICULADO", null, null));

        mockMvc.perform(post("/estudiantes")
                        .header("X-User-Email", ADMIN_EMAIL)
                        .header("X-User-Roles", STAFF_ROLES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateBody()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/estudiantes/7")))
                .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    @DisplayName("POST con cedula formato invalido -> 400 validation")
    void postValidationFail() throws Exception {
        String invalidBody = objectMapper.writeValueAsString(Map.of(
                "cedula", "ABC",
                "nombre", "X",
                "apellido", "Y",
                "email", "x@y.com",
                "telefono", "0987654321",
                "fechaNacimiento", "2000-01-01"
        ));

        mockMvc.perform(post("/estudiantes")
                        .header("X-User-Email", ADMIN_EMAIL)
                        .header("X-User-Roles", ADMIN_ROLES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("https://escuela.local/errors/validation"))
                .andExpect(jsonPath("$.errors.cedula").exists());
    }

    @Test
    @DisplayName("POST con cedula digito verificador invalido -> 400 cedula-invalida")
    void postCedulaInvalida() throws Exception {
        when(service.create(any())).thenThrow(new CedulaInvalidaException("1710034066"));

        mockMvc.perform(post("/estudiantes")
                        .header("X-User-Email", ADMIN_EMAIL)
                        .header("X-User-Roles", ADMIN_ROLES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateBody().replace("1710034065", "1710034066")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("https://escuela.local/errors/cedula-invalida"));
    }

    @Test
    @DisplayName("POST con cedula duplicada -> 409")
    void postCedulaDuplicada() throws Exception {
        when(service.create(any())).thenThrow(new CedulaDuplicadaException("1710034065"));

        mockMvc.perform(post("/estudiantes")
                        .header("X-User-Email", ADMIN_EMAIL)
                        .header("X-User-Roles", ADMIN_ROLES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateBody()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value("https://escuela.local/errors/cedula-duplicada"));
    }

    @Test
    @DisplayName("POST con email duplicado -> 409")
    void postEmailDuplicado() throws Exception {
        when(service.create(any())).thenThrow(new EmailDuplicadoException("h@t.com"));

        mockMvc.perform(post("/estudiantes")
                        .header("X-User-Email", ADMIN_EMAIL)
                        .header("X-User-Roles", ADMIN_ROLES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateBody()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value("https://escuela.local/errors/email-duplicado"));
    }

    // -------------------------------------------------------------------
    // PUT
    // -------------------------------------------------------------------

    @Test
    @DisplayName("PUT existente -> 200")
    void putOk() throws Exception {
        when(service.update(eq(1L), any())).thenReturn(new EstudianteResponse(
                1L, "1710034065", "Nuevo", "Jurado", "h@t.com", "0987654321",
                "ACTIVO", null, null));

        mockMvc.perform(put("/estudiantes/1")
                        .header("X-User-Email", ADMIN_EMAIL)
                        .header("X-User-Roles", ADMIN_ROLES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Nuevo\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Nuevo"));
    }

    // -------------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------------

    @Test
    @DisplayName("DELETE con rol ADMIN -> 204")
    void deleteOk() throws Exception {
        mockMvc.perform(delete("/estudiantes/1")
                        .header("X-User-Email", ADMIN_EMAIL)
                        .header("X-User-Roles", ADMIN_ROLES))
                .andExpect(status().isNoContent());

        verify(service, times(1)).softDelete(1L);
    }

    @Test
    @DisplayName("DELETE con rol STAFF -> 403 (solo ADMIN puede borrar)")
    void deleteStaffForbidden() throws Exception {
        mockMvc.perform(delete("/estudiantes/1")
                        .header("X-User-Email", ADMIN_EMAIL)
                        .header("X-User-Roles", STAFF_ROLES))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE inexistente -> 404")
    void deleteNotFound() throws Exception {
        org.mockito.Mockito.doThrow(new EstudianteNotFoundException(999L))
                .when(service).softDelete(999L);

        mockMvc.perform(delete("/estudiantes/999")
                        .header("X-User-Email", ADMIN_EMAIL)
                        .header("X-User-Roles", ADMIN_ROLES))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------

    private String validCreateBody() throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "cedula", "1710034065",
                "nombre", "Hernan",
                "apellido", "Jurado",
                "email", "h@t.com",
                "telefono", "0987654321",
                "fechaNacimiento", "2000-01-01"
        ));
    }
}
