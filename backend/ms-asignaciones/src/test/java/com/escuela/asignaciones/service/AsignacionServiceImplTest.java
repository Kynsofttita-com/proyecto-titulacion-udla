package com.escuela.asignaciones.service;

import com.escuela.asignaciones.dto.AsignacionResponse;
import com.escuela.asignaciones.dto.CreateAsignacionRequest;
import com.escuela.asignaciones.entity.Asignacion;
import com.escuela.asignaciones.exception.AsignacionNotFoundException;
import com.escuela.asignaciones.exception.DisponibilidadException;
import com.escuela.asignaciones.mapper.AsignacionMapper;
import com.escuela.asignaciones.repository.AsignacionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    private AsignacionMapper mapper;

    @InjectMocks
    private AsignacionServiceImpl service;

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
        CreateAsignacionRequest request = new CreateAsignacionRequest(
                1L, 1L, 1L, LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(11, 0), null
        );

        Asignacion asignacion = Asignacion.builder()
                .id(1L)
                .estudianteId(1L)
                .instructorId(1L)
                .vehiculoId(1L)
                .build();

        AsignacionResponse response = new AsignacionResponse(
                1L, 1L, 1L, 1L, LocalDate.now(), LocalTime.of(10, 0),
                LocalTime.of(11, 0), "CONFIRMADA", null, LocalDateTime.now(), LocalDateTime.now()
        );

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
    }

    @Test
    void testCreate_InstructorNoDisponible() {
        CreateAsignacionRequest request = new CreateAsignacionRequest(
                1L, 1L, 1L, LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(11, 0), null
        );

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
}
