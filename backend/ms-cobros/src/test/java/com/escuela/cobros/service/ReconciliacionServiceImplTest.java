package com.escuela.cobros.service;

import com.escuela.cobros.dto.ReconciliacionListResponse;
import com.escuela.cobros.dto.ReconciliacionRequest;
import com.escuela.cobros.dto.ReconciliacionResponse;
import com.escuela.cobros.entity.Reconciliacion;
import com.escuela.cobros.mapper.ReconciliacionMapper;
import com.escuela.cobros.repository.ReconciliacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReconciliacionServiceImplTest {

    @Mock
    private ReconciliacionRepository reconciliacionRepository;

    @Mock
    private ReconciliacionMapper reconciliacionMapper;

    @InjectMocks
    private ReconciliacionServiceImpl reconciliacionService;

    private Reconciliacion reconciliacionMock;
    private ReconciliacionRequest reconciliacionRequest;
    private ReconciliacionResponse reconciliacionResponse;
    private ReconciliacionListResponse reconciliacionListResponse;

    @BeforeEach
    void setUp() {
        LocalDate fecha = LocalDate.now();

        reconciliacionMock = new Reconciliacion();
        reconciliacionMock.setId(1L);
        reconciliacionMock.setFecha(fecha);
        reconciliacionMock.setTotalEfectivo(BigDecimal.valueOf(5000));
        reconciliacionMock.setTotalTarjeta(BigDecimal.valueOf(3000));
        reconciliacionMock.setTotalTransferencia(BigDecimal.valueOf(2000));
        reconciliacionMock.setTotalCheque(BigDecimal.ZERO);
        reconciliacionMock.setCantidadPagos(10);
        reconciliacionMock.setObservaciones("Reconciliación diaria");
        reconciliacionMock.setCreatedAt(LocalDateTime.now());

        reconciliacionRequest = new ReconciliacionRequest(
            fecha,
            BigDecimal.valueOf(5000),
            BigDecimal.valueOf(3000),
            BigDecimal.valueOf(2000),
            BigDecimal.ZERO,
            10,
            "Reconciliación diaria"
        );

        reconciliacionResponse = new ReconciliacionResponse(
            1L,
            fecha,
            BigDecimal.valueOf(5000),
            BigDecimal.valueOf(3000),
            BigDecimal.valueOf(2000),
            BigDecimal.ZERO,
            BigDecimal.valueOf(10000),
            10,
            "Reconciliación diaria",
            null,
            LocalDateTime.now(),
            null,
            null,
            null
        );

        reconciliacionListResponse = new ReconciliacionListResponse(
            1L,
            fecha,
            BigDecimal.valueOf(10000),
            10,
            LocalDateTime.now()
        );
    }

    @Test
    void testFindAll_Success() {
        Pageable pageable = PageRequest.of(0, 50);
        Page<Reconciliacion> page = new PageImpl<>(List.of(reconciliacionMock));

        when(reconciliacionRepository.findAllByOrderByFechaDesc(pageable))
            .thenReturn(page);
        when(reconciliacionMapper.toListResponse(reconciliacionMock))
            .thenReturn(reconciliacionListResponse);

        Page<ReconciliacionListResponse> result = reconciliacionService.findAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(reconciliacionRepository, times(1)).findAllByOrderByFechaDesc(pageable);
    }

    @Test
    void testFindById_Success() {
        when(reconciliacionRepository.findById(1L))
            .thenReturn(Optional.of(reconciliacionMock));
        when(reconciliacionMapper.toResponse(reconciliacionMock))
            .thenReturn(reconciliacionResponse);

        ReconciliacionResponse result = reconciliacionService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        verify(reconciliacionRepository, times(1)).findById(1L);
    }

    @Test
    void testFindById_NotFound() {
        when(reconciliacionRepository.findById(999L))
            .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> reconciliacionService.findById(999L));
        verify(reconciliacionRepository, times(1)).findById(999L);
    }

    @Test
    void testFindByFecha_Success() {
        LocalDate fecha = LocalDate.now();

        when(reconciliacionRepository.findByFecha(fecha))
            .thenReturn(Optional.of(reconciliacionMock));
        when(reconciliacionMapper.toResponse(reconciliacionMock))
            .thenReturn(reconciliacionResponse);

        Optional<ReconciliacionResponse> result = reconciliacionService.findByFecha(fecha);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().id());
        verify(reconciliacionRepository, times(1)).findByFecha(fecha);
    }

    @Test
    void testFindByFecha_NotFound() {
        LocalDate fecha = LocalDate.now();

        when(reconciliacionRepository.findByFecha(fecha))
            .thenReturn(Optional.empty());

        Optional<ReconciliacionResponse> result = reconciliacionService.findByFecha(fecha);

        assertTrue(result.isEmpty());
        verify(reconciliacionRepository, times(1)).findByFecha(fecha);
    }

    @Test
    void testCreate_Success() {
        when(reconciliacionRepository.findByFecha(reconciliacionRequest.fecha()))
            .thenReturn(Optional.empty());
        when(reconciliacionMapper.toEntity(reconciliacionRequest))
            .thenReturn(reconciliacionMock);
        when(reconciliacionRepository.save(reconciliacionMock))
            .thenReturn(reconciliacionMock);
        when(reconciliacionMapper.toResponse(reconciliacionMock))
            .thenReturn(reconciliacionResponse);

        ReconciliacionResponse result = reconciliacionService.create(reconciliacionRequest);

        assertNotNull(result);
        assertEquals(1L, result.id());
        verify(reconciliacionRepository, times(1)).findByFecha(reconciliacionRequest.fecha());
        verify(reconciliacionRepository, times(1)).save(any(Reconciliacion.class));
    }

    @Test
    void testCreate_FechaAlreadyExists() {
        LocalDate fecha = LocalDate.now();
        ReconciliacionRequest request = new ReconciliacionRequest(
            fecha,
            BigDecimal.valueOf(5000),
            BigDecimal.valueOf(3000),
            BigDecimal.valueOf(2000),
            BigDecimal.ZERO,
            10,
            "Reconciliación duplicada"
        );

        when(reconciliacionRepository.findByFecha(fecha))
            .thenReturn(Optional.of(reconciliacionMock));

        assertThrows(RuntimeException.class, () -> reconciliacionService.create(request));
        verify(reconciliacionRepository, times(1)).findByFecha(fecha);
        verify(reconciliacionRepository, never()).save(any(Reconciliacion.class));
    }

    @Test
    void testUpdate_Success() {
        when(reconciliacionRepository.findById(1L))
            .thenReturn(Optional.of(reconciliacionMock));
        doNothing().when(reconciliacionMapper).updateEntity(reconciliacionRequest, reconciliacionMock);
        when(reconciliacionRepository.save(reconciliacionMock))
            .thenReturn(reconciliacionMock);
        when(reconciliacionMapper.toResponse(reconciliacionMock))
            .thenReturn(reconciliacionResponse);

        ReconciliacionResponse result = reconciliacionService.update(1L, reconciliacionRequest);

        assertNotNull(result);
        assertEquals(1L, result.id());
        verify(reconciliacionRepository, times(1)).findById(1L);
        verify(reconciliacionRepository, times(1)).save(reconciliacionMock);
    }

    @Test
    void testUpdate_NotFound() {
        when(reconciliacionRepository.findById(999L))
            .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> reconciliacionService.update(999L, reconciliacionRequest));
        verify(reconciliacionRepository, times(1)).findById(999L);
        verify(reconciliacionRepository, never()).save(any(Reconciliacion.class));
    }

    @Test
    void testDelete_Success() {
        when(reconciliacionRepository.existsById(1L))
            .thenReturn(true);
        doNothing().when(reconciliacionRepository).deleteById(1L);

        assertDoesNotThrow(() -> reconciliacionService.delete(1L));
        verify(reconciliacionRepository, times(1)).existsById(1L);
        verify(reconciliacionRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDelete_NotFound() {
        when(reconciliacionRepository.existsById(999L))
            .thenReturn(false);

        assertThrows(RuntimeException.class, () -> reconciliacionService.delete(999L));
        verify(reconciliacionRepository, times(1)).existsById(999L);
        verify(reconciliacionRepository, never()).deleteById(any());
    }
}
