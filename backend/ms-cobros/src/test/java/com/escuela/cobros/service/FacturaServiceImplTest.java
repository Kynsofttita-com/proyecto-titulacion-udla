package com.escuela.cobros.service;

import com.escuela.cobros.client.EstudianteClient;
import com.escuela.cobros.dto.EstudianteDetailDTO;
import com.escuela.cobros.dto.FacturaListResponse;
import com.escuela.cobros.dto.FacturaRequest;
import com.escuela.cobros.dto.FacturaResponse;
import com.escuela.cobros.entity.Factura;
import com.escuela.cobros.exception.EstudianteInactivoException;
import com.escuela.cobros.exception.EstudianteNotFoundException;
import com.escuela.cobros.exception.FacturaNotFoundException;
import com.escuela.cobros.mapper.FacturaMapper;
import com.escuela.cobros.repository.FacturaRepository;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FacturaServiceImplTest {

    @Mock
    private FacturaRepository facturaRepository;

    @Mock
    private FacturaMapper facturaMapper;

    @Mock
    private EstudianteClient estudianteClient;

    @Mock
    private FacturaEventDispatcher eventDispatcher;

    @InjectMocks
    private FacturaServiceImpl facturaService;

    private Factura facturaMock;
    private FacturaRequest facturaRequest;
    private FacturaResponse facturaResponse;

    @BeforeEach
    void setUp() {
        facturaMock = new Factura();
        facturaMock.setId(1L);
        facturaMock.setNumeroFactura("FAC-0001");
        facturaMock.setEstudianteId(1L);
        facturaMock.setMontoOriginal(BigDecimal.valueOf(1000));
        facturaMock.setMontoPagado(BigDecimal.ZERO);
        facturaMock.setEstado("PENDIENTE");
        facturaMock.setFechaVencimiento(LocalDate.now().plusMonths(1));

        facturaRequest = new FacturaRequest(
            1L,
            1L,
            BigDecimal.valueOf(1000),
            LocalDate.now().plusMonths(1),
            "Curso básico",
            "CONTADO",
            1,
            null,
            null
        );

        facturaResponse = new FacturaResponse(
            1L,
            "FAC-0001",
            1L,
            1L,
            BigDecimal.valueOf(1000),
            BigDecimal.ZERO,
            BigDecimal.valueOf(1000),
            "PENDIENTE",
            LocalDate.now(),
            LocalDate.now().plusMonths(1),
            "Curso básico",
            "CONTADO",
            1,
            0,
            null,
            null,
            BigDecimal.valueOf(1000),
            null,
            null,
            null,
            null
        );
    }

    @Test
    void testFindById_Success() {
        when(facturaRepository.findByIdAndDeletedAtIsNull(1L))
            .thenReturn(Optional.of(facturaMock));
        when(facturaMapper.toResponse(facturaMock))
            .thenReturn(facturaResponse);

        FacturaResponse result = facturaService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        verify(facturaRepository, times(1)).findByIdAndDeletedAtIsNull(1L);
        verify(facturaMapper, times(1)).toResponse(facturaMock);
    }

    @Test
    void testFindById_NotFound() {
        when(facturaRepository.findByIdAndDeletedAtIsNull(999L))
            .thenReturn(Optional.empty());

        assertThrows(FacturaNotFoundException.class, () -> facturaService.findById(999L));
        verify(facturaRepository, times(1)).findByIdAndDeletedAtIsNull(999L);
    }

    @Test
    void testCreate_Success() {
        when(estudianteClient.obtenerEstudiante(1L))
            .thenReturn(new EstudianteDetailDTO(1L, "ACTIVO"));
        when(facturaRepository.findMaxNumeroFactura())
            .thenReturn(0L);
        when(facturaMapper.toEntity(facturaRequest))
            .thenReturn(facturaMock);
        when(facturaRepository.save(any(Factura.class)))
            .thenReturn(facturaMock);
        when(facturaMapper.toResponse(facturaMock))
            .thenReturn(facturaResponse);

        FacturaResponse result = facturaService.create(facturaRequest);

        assertNotNull(result);
        assertEquals("PENDIENTE", result.estado());
        verify(estudianteClient, times(1)).obtenerEstudiante(1L);
        verify(facturaRepository, times(1)).save(any(Factura.class));
    }

    @Test
    void testCreate_EstudianteNoEncontrado() {
        when(estudianteClient.obtenerEstudiante(999L))
            .thenThrow(new RuntimeException("Student not found"));

        FacturaRequest request = new FacturaRequest(
            999L,
            1L,
            BigDecimal.valueOf(1000),
            LocalDate.now().plusMonths(1),
            "Curso",
            "CONTADO",
            1,
            null,
            null
        );

        assertThrows(EstudianteNotFoundException.class, () -> facturaService.create(request));
    }

    @Test
    void testCreate_EstudianteInactivo() {
        when(estudianteClient.obtenerEstudiante(1L))
            .thenReturn(new EstudianteDetailDTO(1L, "INACTIVO"));

        assertThrows(EstudianteInactivoException.class, () -> facturaService.create(facturaRequest));
    }

    @Test
    void testFindAll() {
        Pageable pageable = PageRequest.of(0, 50);
        Page<Factura> page = new PageImpl<>(List.of(facturaMock));
        Page<FacturaListResponse> expected = new PageImpl<>(
            List.of(new FacturaListResponse(1L, "FAC-0001", 1L,
                BigDecimal.valueOf(1000), BigDecimal.ZERO,
                BigDecimal.valueOf(1000), "PENDIENTE", LocalDate.now(), LocalDate.now().plusDays(30), "CONTADO", 1, 0, null))
        );

        when(facturaRepository.findByDeletedAtIsNull(pageable))
            .thenReturn(page);
        when(facturaMapper.toListResponse(facturaMock))
            .thenReturn(expected.getContent().get(0));

        Page<FacturaListResponse> result = facturaService.findAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(facturaRepository, times(1)).findByDeletedAtIsNull(pageable);
    }

    @Test
    void testUpdate_Success() {
        when(facturaRepository.findByIdAndDeletedAtIsNull(1L))
            .thenReturn(Optional.of(facturaMock));
        when(facturaRepository.save(any(Factura.class)))
            .thenReturn(facturaMock);
        when(facturaMapper.toResponse(facturaMock))
            .thenReturn(facturaResponse);
        when(estudianteClient.obtenerEstudiante(1L))
            .thenReturn(new EstudianteDetailDTO(1L, "ACTIVO"));

        FacturaResponse result = facturaService.update(1L, facturaRequest);

        assertNotNull(result);
        verify(facturaRepository, times(1)).findByIdAndDeletedAtIsNull(1L);
        verify(facturaRepository, times(1)).save(any(Factura.class));
        verify(facturaMapper, times(1)).updateEntity(eq(facturaRequest), any(Factura.class));
    }

    @Test
    void testSoftDelete_Success() {
        when(facturaRepository.findByIdAndDeletedAtIsNull(1L))
            .thenReturn(Optional.of(facturaMock));
        when(facturaRepository.save(any(Factura.class)))
            .thenReturn(facturaMock);

        facturaService.softDelete(1L);

        verify(facturaRepository, times(1)).findByIdAndDeletedAtIsNull(1L);
        verify(facturaRepository, times(1)).save(any(Factura.class));
        verify(eventDispatcher, times(1)).publishCancelada(facturaMock);
    }

    @Test
    void testFindByEstudianteId() {
        Pageable pageable = PageRequest.of(0, 50);
        Page<Factura> page = new PageImpl<>(List.of(facturaMock));

        when(facturaRepository.findByEstudianteIdAndDeletedAtIsNull(1L, pageable))
            .thenReturn(page);
        when(facturaMapper.toListResponse(facturaMock))
            .thenReturn(new FacturaListResponse(1L, "FAC-0001", 1L,
                BigDecimal.valueOf(1000), BigDecimal.ZERO,
                BigDecimal.valueOf(1000), "PENDIENTE", LocalDate.now(), LocalDate.now().plusDays(30), "CONTADO", 1, 0, null));

        Page<FacturaListResponse> result = facturaService.findByEstudianteId(1L, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(facturaRepository, times(1)).findByEstudianteIdAndDeletedAtIsNull(1L, pageable);
    }
}
