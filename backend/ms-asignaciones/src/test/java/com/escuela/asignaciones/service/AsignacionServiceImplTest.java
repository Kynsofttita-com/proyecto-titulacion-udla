package com.escuela.asignaciones.service;

import com.escuela.asignaciones.dto.AsignacionResponse;
import com.escuela.asignaciones.dto.CreateAsignacionRequest;
import com.escuela.asignaciones.dto.UpdateAsignacionReprogramarRequest;
import com.escuela.asignaciones.dto.feign.DisponibilidadDelDiaDTO;
import com.escuela.asignaciones.dto.feign.EstudianteDetailDTO;
import com.escuela.asignaciones.dto.feign.InstructorDetailDTO;
import com.escuela.asignaciones.dto.feign.VehiculoDetailDTO;
import com.escuela.asignaciones.entity.Asignacion;
import com.escuela.asignaciones.exception.*;
import com.escuela.asignaciones.feign.EstudianteClient;
import com.escuela.asignaciones.feign.InstructorClient;
import com.escuela.asignaciones.feign.VehiculoClient;
import com.escuela.asignaciones.mapper.AsignacionMapper;
import com.escuela.asignaciones.repository.AsignacionRepository;
import com.escuela.asignaciones.repository.HistorialEstadoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsignacionServiceImplTest {

    @Mock
    private AsignacionRepository repository;

    @Mock
    private HistorialEstadoRepository historialRepository;

    @Mock
    private AsignacionMapper mapper;

    @Mock
    private EstudianteClient estudianteClient;

    @Mock
    private InstructorClient instructorClient;

    @Mock
    private VehiculoClient vehiculoClient;

    @Mock
    private AsignacionEventDispatcher eventDispatcher;

    @InjectMocks
    private AsignacionServiceImpl service;

    @BeforeEach
    void setUp() {
        // AsignacionServiceImpl usa lazy init para 'mapper' (no esta en el
        // constructor). @InjectMocks no lo establece con constructor injection
        // completo; forzamos field injection para que los stubs funcionen.
        ReflectionTestUtils.setField(service, "mapper", mapper);
    }

    @Test
    void testFindById_Success() {
        LocalDateTime fechaHora = LocalDateTime.now();
        Asignacion asignacion = Asignacion.builder()
                .id(1L)
                .estudianteId(1L)
                .instructorId(1L)
                .vehiculoId(1L)
                .fechaHora(fechaHora)
                .duracionMinutos((short)60)
                .estado("CONFIRMADA")
                .createdAt(LocalDateTime.now())
                .build();

        AsignacionResponse response = new AsignacionResponse(
                1L, 1L, 1L, 1L, fechaHora.toLocalDate(), fechaHora.toLocalTime(),
                fechaHora.plusMinutes(60).toLocalTime(), "CONFIRMADA", null, LocalDateTime.now(), LocalDateTime.now()
        );

        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(asignacion));
        when(mapper.toResponse(asignacion)).thenReturn(response);

        AsignacionResponse result = service.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        verify(repository, times(1)).findByIdAndDeletedAtIsNull(1L);
    }

    @Test
    void testFindById_NotFound() {
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());
        assertThrows(AsignacionNotFoundException.class, () -> service.findById(1L));
    }

    @Test
    void testCreate_Success() {
        LocalDate hoy = LocalDate.now();
        CreateAsignacionRequest request = new CreateAsignacionRequest(
                1L, 1L, 1L, hoy, LocalTime.of(10, 0), LocalTime.of(11, 0), null
        );

        Asignacion asignacion = Asignacion.builder()
                .id(1L)
                .estudianteId(1L)
                .instructorId(1L)
                .vehiculoId(1L)
                .fechaHora(hoy.atTime(LocalTime.of(10, 0)))
                .duracionMinutos((short)60)
                .estado("CONFIRMADA")
                .build();

        AsignacionResponse response = new AsignacionResponse(
                1L, 1L, 1L, 1L, hoy, LocalTime.of(10, 0),
                LocalTime.of(11, 0), "CONFIRMADA", null, LocalDateTime.now(), LocalDateTime.now()
        );

        when(estudianteClient.obtenerEstudiante(1L))
                .thenReturn(new EstudianteDetailDTO(1L, "MATRICULADO", 1L, 0));
        when(instructorClient.obtenerInstructor(1L))
                .thenReturn(new InstructorDetailDTO(1L, "ACTIVO"));
        when(vehiculoClient.obtenerVehiculo(1L))
                .thenReturn(new VehiculoDetailDTO(
                        1L, "ABC-1234", "ACTIVO", 0, 1L,
                        LocalDate.now().plusYears(1), LocalDate.now().plusYears(1), null));
        when(instructorClient.obtenerDisponibilidad(eq(1L), any(LocalDate.class)))
                .thenReturn(new DisponibilidadDelDiaDTO(
                        1L, hoy.toString(), (short) hoy.getDayOfWeek().getValue(),
                        true, null,
                        List.of(new DisponibilidadDelDiaDTO.FranjaHoraria(
                                LocalTime.of(8, 0), LocalTime.of(18, 0)))));
        when(repository.countByInstructorIdAndFechaHoraBetweenAndEstadoAndDeletedAtIsNull(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class), eq("CONFIRMADA"))).thenReturn(0L);
        when(repository.countByVehiculoIdAndFechaHoraBetweenAndEstadoAndDeletedAtIsNull(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class), eq("CONFIRMADA"))).thenReturn(0L);
        when(mapper.toEntity(request)).thenReturn(asignacion);
        when(repository.save(any())).thenReturn(asignacion);
        when(mapper.toResponse(asignacion)).thenReturn(response);

        AsignacionResponse result = service.create(request);

        assertNotNull(result);
        assertEquals(1L, result.id());
        verify(repository, times(1)).save(any());
        verify(eventDispatcher, times(1)).publishCreada(any());
    }

    @Test
    void testCreate_EstudianteNoEncontrado() {
        CreateAsignacionRequest request = new CreateAsignacionRequest(
                999L, 1L, 1L, LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(11, 0), null
        );

        when(estudianteClient.obtenerEstudiante(999L))
                .thenThrow(new RuntimeException("Not found"));

        assertThrows(EstudianteNoEncontradoException.class, () -> service.create(request));
    }

    @Test
    void testCreate_EstudianteInactivo() {
        CreateAsignacionRequest request = new CreateAsignacionRequest(
                1L, 1L, 1L, LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(11, 0), null
        );

        when(estudianteClient.obtenerEstudiante(1L))
                .thenReturn(new EstudianteDetailDTO(1L, "PRE_MATRICULADO", null, 0));

        assertThrows(EstudianteInactivoException.class, () -> service.create(request));
    }

    @Test
    void testCreate_InstructorNoDisponible() {
        LocalDate hoy = LocalDate.now();
        CreateAsignacionRequest request = new CreateAsignacionRequest(
                1L, 1L, 1L, hoy, LocalTime.of(10, 0), LocalTime.of(11, 0), null
        );

        when(estudianteClient.obtenerEstudiante(1L))
                .thenReturn(new EstudianteDetailDTO(1L, "MATRICULADO", 1L, 0));
        when(instructorClient.obtenerInstructor(1L))
                .thenReturn(new InstructorDetailDTO(1L, "ACTIVO"));
        when(vehiculoClient.obtenerVehiculo(1L))
                .thenReturn(new VehiculoDetailDTO(
                        1L, "ABC-1234", "ACTIVO", 0, 1L,
                        LocalDate.now().plusYears(1), LocalDate.now().plusYears(1), null));
        when(instructorClient.obtenerDisponibilidad(eq(1L), any(LocalDate.class)))
                .thenReturn(new DisponibilidadDelDiaDTO(
                        1L, hoy.toString(), (short) hoy.getDayOfWeek().getValue(),
                        true, null,
                        List.of(new DisponibilidadDelDiaDTO.FranjaHoraria(
                                LocalTime.of(8, 0), LocalTime.of(18, 0)))));
        when(repository.countByInstructorIdAndFechaHoraBetweenAndEstadoAndDeletedAtIsNull(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class), eq("CONFIRMADA"))).thenReturn(1L);

        assertThrows(DisponibilidadException.class, () -> service.create(request));
    }

    @Test
    void testSoftDelete_Success() {
        Asignacion asignacion = Asignacion.builder().id(1L).build();
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(asignacion));

        service.softDelete(1L);

        verify(repository, times(1)).save(any());
    }

    @Test
    void testReprogramar_Success() {
        LocalDateTime fechaHoraAnterior = LocalDateTime.of(2026, 6, 1, 9, 0);
        Asignacion asignacion = Asignacion.builder()
                .id(1L)
                .estudianteId(1L)
                .instructorId(1L)
                .vehiculoId(1L)
                .fechaHora(fechaHoraAnterior)
                .duracionMinutos((short)60)
                .estado("CONFIRMADA")
                .build();

        UpdateAsignacionReprogramarRequest request = new UpdateAsignacionReprogramarRequest(
                LocalDate.of(2026, 6, 5), LocalTime.of(10, 0), LocalTime.of(11, 0), null
        );

        AsignacionResponse response = new AsignacionResponse(
                1L, 1L, 1L, 1L, LocalDate.of(2026, 6, 5), LocalTime.of(10, 0),
                LocalTime.of(11, 0), "CONFIRMADA", null, LocalDateTime.now(), LocalDateTime.now()
        );

        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(asignacion));
        when(repository.countByInstructorIdAndFechaHoraBetweenAndEstadoAndDeletedAtIsNullAndIdNot(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class), eq("CONFIRMADA"), eq(1L))).thenReturn(0L);
        when(repository.countByVehiculoIdAndFechaHoraBetweenAndEstadoAndDeletedAtIsNullAndIdNot(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class), eq("CONFIRMADA"), eq(1L))).thenReturn(0L);
        when(repository.save(any())).thenReturn(asignacion);
        when(mapper.toResponse(asignacion)).thenReturn(response);
        when(historialRepository.save(any())).thenReturn(null);

        AsignacionResponse result = service.reprogramar(1L, request);

        assertNotNull(result);
        assertEquals(1L, result.id());
        verify(repository, times(1)).save(any());
        verify(eventDispatcher, times(1)).publishReprogramada(eq(asignacion), eq(fechaHoraAnterior));
        verify(historialRepository, times(1)).save(any());
    }

    @Test
    void testReprogramar_NotFound() {
        UpdateAsignacionReprogramarRequest request = new UpdateAsignacionReprogramarRequest(
                LocalDate.of(2026, 6, 5), LocalTime.of(10, 0), LocalTime.of(11, 0), null
        );
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        assertThrows(AsignacionNotFoundException.class, () -> service.reprogramar(1L, request));
    }

    @Test
    void testReprogramar_ConflictInstructor() {
        LocalDateTime fechaHoraAnterior = LocalDateTime.of(2026, 6, 1, 9, 0);
        Asignacion asignacion = Asignacion.builder()
                .id(1L)
                .estudianteId(1L)
                .instructorId(1L)
                .vehiculoId(1L)
                .fechaHora(fechaHoraAnterior)
                .duracionMinutos((short)60)
                .estado("CONFIRMADA")
                .build();

        UpdateAsignacionReprogramarRequest request = new UpdateAsignacionReprogramarRequest(
                LocalDate.of(2026, 6, 5), LocalTime.of(10, 0), LocalTime.of(11, 0), null
        );

        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(asignacion));
        when(repository.countByInstructorIdAndFechaHoraBetweenAndEstadoAndDeletedAtIsNullAndIdNot(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class), eq("CONFIRMADA"), eq(1L))).thenReturn(1L);

        assertThrows(DisponibilidadException.class, () -> service.reprogramar(1L, request));
    }

    @Test
    void testReprogramar_ConflictVehiculo() {
        LocalDateTime fechaHoraAnterior = LocalDateTime.of(2026, 6, 1, 9, 0);
        Asignacion asignacion = Asignacion.builder()
                .id(1L)
                .estudianteId(1L)
                .instructorId(1L)
                .vehiculoId(1L)
                .fechaHora(fechaHoraAnterior)
                .duracionMinutos((short)60)
                .estado("CONFIRMADA")
                .build();

        UpdateAsignacionReprogramarRequest request = new UpdateAsignacionReprogramarRequest(
                LocalDate.of(2026, 6, 5), LocalTime.of(10, 0), LocalTime.of(11, 0), null
        );

        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(asignacion));
        when(repository.countByInstructorIdAndFechaHoraBetweenAndEstadoAndDeletedAtIsNullAndIdNot(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class), eq("CONFIRMADA"), eq(1L))).thenReturn(0L);
        when(repository.countByVehiculoIdAndFechaHoraBetweenAndEstadoAndDeletedAtIsNullAndIdNot(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class), eq("CONFIRMADA"), eq(1L))).thenReturn(1L);

        assertThrows(DisponibilidadException.class, () -> service.reprogramar(1L, request));
    }
}
