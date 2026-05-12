package com.escuela.estudiantes.service;

import com.escuela.estudiantes.dto.CreateEstudianteRequest;
import com.escuela.estudiantes.dto.EstudianteDetailResponse;
import com.escuela.estudiantes.dto.EstudianteResponse;
import com.escuela.estudiantes.dto.UpdateEstudianteRequest;
import com.escuela.estudiantes.entity.Estudiante;
import com.escuela.estudiantes.exception.CedulaDuplicadaException;
import com.escuela.estudiantes.exception.CedulaInvalidaException;
import com.escuela.estudiantes.exception.EmailDuplicadoException;
import com.escuela.estudiantes.exception.EstudianteNotFoundException;
import com.escuela.estudiantes.mapper.EstudianteMapper;
import com.escuela.estudiantes.repository.EstudianteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EstudianteServiceImpl")
class EstudianteServiceImplTest {

    private static final String CEDULA_VALIDA = "1710034065";
    private static final String CEDULA_INVALIDA = "1710034066";

    @Mock
    private EstudianteRepository repository;

    @Mock
    private EstudianteMapper mapper;

    @InjectMocks
    private EstudianteServiceImpl service;

    private CreateEstudianteRequest createRequest;
    private Estudiante entidad;

    @BeforeEach
    void setUp() {
        createRequest = new CreateEstudianteRequest(
                CEDULA_VALIDA, "Hernan", "Jurado", "hernan@test.com",
                "0987654321", "Av Siempre Viva 123",
                LocalDate.of(2000, 1, 1), "M",
                null, null, null, null
        );

        entidad = Estudiante.builder()
                .id(1L)
                .cedula(CEDULA_VALIDA)
                .nombre("Hernan")
                .apellido("Jurado")
                .email("hernan@test.com")
                .telefono("0987654321")
                .fechaNacimiento(LocalDate.of(2000, 1, 1))
                .estado("PRE_MATRICULADO")
                .contactosEmergencia(new HashSet<>())
                .build();
    }

    // -------------------------------------------------------------------
    // create
    // -------------------------------------------------------------------

    @Test
    @DisplayName("create con cedula valida y unicidad OK -> retorna response")
    void createOk() {
        when(repository.existsByCedulaAndDeletedAtIsNull(CEDULA_VALIDA)).thenReturn(false);
        when(repository.existsByEmailAndDeletedAtIsNull("hernan@test.com")).thenReturn(false);
        when(mapper.toEntity(createRequest)).thenReturn(entidad);
        when(repository.save(any(Estudiante.class))).thenReturn(entidad);
        when(mapper.toResponse(entidad)).thenReturn(new EstudianteResponse(
                1L, CEDULA_VALIDA, "Hernan", "Jurado", "hernan@test.com",
                "0987654321", "PRE_MATRICULADO", null));

        EstudianteResponse response = service.create(createRequest);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.cedula()).isEqualTo(CEDULA_VALIDA);
        verify(repository).save(any(Estudiante.class));
    }

    @Test
    @DisplayName("create con cedula con digito verificador incorrecto -> CedulaInvalidaException")
    void createCedulaInvalida() {
        CreateEstudianteRequest req = new CreateEstudianteRequest(
                CEDULA_INVALIDA, "X", "Y", "x@y.com", "0987654321", null,
                LocalDate.of(2000, 1, 1), "M", null, null, null, null);

        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(CedulaInvalidaException.class)
                .hasMessageContaining(CEDULA_INVALIDA);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create con cedula ya existente -> CedulaDuplicadaException")
    void createCedulaDuplicada() {
        when(repository.existsByCedulaAndDeletedAtIsNull(CEDULA_VALIDA)).thenReturn(true);

        assertThatThrownBy(() -> service.create(createRequest))
                .isInstanceOf(CedulaDuplicadaException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create con email ya existente -> EmailDuplicadoException")
    void createEmailDuplicado() {
        when(repository.existsByCedulaAndDeletedAtIsNull(CEDULA_VALIDA)).thenReturn(false);
        when(repository.existsByEmailAndDeletedAtIsNull("hernan@test.com")).thenReturn(true);

        assertThatThrownBy(() -> service.create(createRequest))
                .isInstanceOf(EmailDuplicadoException.class);

        verify(repository, never()).save(any());
    }

    // -------------------------------------------------------------------
    // findById
    // -------------------------------------------------------------------

    @Test
    @DisplayName("findById existente -> DetailResponse")
    void findByIdOk() {
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(entidad));
        when(mapper.toDetailResponse(entidad)).thenReturn(new EstudianteDetailResponse(
                1L, CEDULA_VALIDA, "Hernan", "Jurado", "hernan@test.com",
                "0987654321", null, LocalDate.of(2000, 1, 1), "M",
                "PRE_MATRICULADO", null, null, null, null, null,
                null, null, null, null));

        EstudianteDetailResponse response = service.findById(1L);

        assertThat(response.id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findById inexistente -> EstudianteNotFoundException")
    void findByIdNotFound() {
        when(repository.findByIdAndDeletedAtIsNull(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(999L))
                .isInstanceOf(EstudianteNotFoundException.class)
                .hasMessageContaining("999");
    }

    // -------------------------------------------------------------------
    // update
    // -------------------------------------------------------------------

    @Test
    @DisplayName("update existente con email igual -> OK")
    void updateOk() {
        UpdateEstudianteRequest req = new UpdateEstudianteRequest(
                "HernanNuevo", null, null, null, "Nueva direccion",
                null, null, "ACTIVO", null, null, null);
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(entidad));
        when(repository.save(entidad)).thenReturn(entidad);
        when(mapper.toResponse(entidad)).thenReturn(new EstudianteResponse(
                1L, CEDULA_VALIDA, "HernanNuevo", "Jurado", "hernan@test.com",
                "0987654321", "ACTIVO", null));

        EstudianteResponse response = service.update(1L, req);

        assertThat(response.id()).isEqualTo(1L);
        verify(mapper).updateEntity(entidad, req);
        verify(repository).save(entidad);
    }

    @Test
    @DisplayName("update con email NUEVO ya tomado por otro -> EmailDuplicadoException")
    void updateEmailDuplicado() {
        UpdateEstudianteRequest req = new UpdateEstudianteRequest(
                null, null, "tomado@test.com", null, null, null, null, null, null, null, null);
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(entidad));
        when(repository.existsByEmailAndDeletedAtIsNull("tomado@test.com")).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, req))
                .isInstanceOf(EmailDuplicadoException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update inexistente -> EstudianteNotFoundException")
    void updateNotFound() {
        UpdateEstudianteRequest req = new UpdateEstudianteRequest(
                "X", null, null, null, null, null, null, null, null, null, null);
        when(repository.findByIdAndDeletedAtIsNull(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(999L, req))
                .isInstanceOf(EstudianteNotFoundException.class);
    }

    // -------------------------------------------------------------------
    // softDelete
    // -------------------------------------------------------------------

    @Test
    @DisplayName("softDelete existente -> marca deletedAt y save")
    void softDeleteOk() {
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(entidad));
        when(repository.save(entidad)).thenReturn(entidad);

        service.softDelete(1L);

        assertThat(entidad.getDeletedAt()).isNotNull();
        verify(repository, times(1)).save(entidad);
    }

    @Test
    @DisplayName("softDelete inexistente -> EstudianteNotFoundException")
    void softDeleteNotFound() {
        when(repository.findByIdAndDeletedAtIsNull(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.softDelete(999L))
                .isInstanceOf(EstudianteNotFoundException.class);

        verify(repository, never()).save(any());
    }
}
