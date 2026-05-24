package com.escuela.cobros.service;

import com.escuela.cobros.dto.EstadoCuentaResponse;
import com.escuela.cobros.dto.FacturaListResponse;
import com.escuela.cobros.entity.Factura;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EstadoCuentaServiceImplTest {

    @Mock
    private FacturaRepository facturaRepository;

    @Mock
    private FacturaMapper facturaMapper;

    @InjectMocks
    private EstadoCuentaServiceImpl estadoCuentaService;

    private Factura facturaPendiente;
    private Factura facturaParcial;
    private Factura facturaPagada;
    private Factura facturaVencida;

    @BeforeEach
    void setUp() {
        LocalDateTime ahora = LocalDateTime.now();

        facturaPendiente = new Factura();
        facturaPendiente.setId(1L);
        facturaPendiente.setEstudianteId(1L);
        facturaPendiente.setNumeroFactura("FAC-0001");
        facturaPendiente.setMontoOriginal(BigDecimal.valueOf(1000));
        facturaPendiente.setMontoPagado(BigDecimal.ZERO);
        facturaPendiente.setSaldo(BigDecimal.valueOf(1000));
        facturaPendiente.setEstado("PENDIENTE");
        facturaPendiente.setFechaVencimiento(LocalDate.now().plusDays(10));
        facturaPendiente.setCreatedAt(ahora);
        facturaPendiente.setDeletedAt(null);

        facturaParcial = new Factura();
        facturaParcial.setId(2L);
        facturaParcial.setEstudianteId(1L);
        facturaParcial.setNumeroFactura("FAC-0002");
        facturaParcial.setMontoOriginal(BigDecimal.valueOf(500));
        facturaParcial.setMontoPagado(BigDecimal.valueOf(200));
        facturaParcial.setSaldo(BigDecimal.valueOf(300));
        facturaParcial.setEstado("PARCIAL");
        facturaParcial.setFechaVencimiento(LocalDate.now().plusDays(5));
        facturaParcial.setCreatedAt(ahora.minusDays(1));
        facturaParcial.setDeletedAt(null);

        facturaPagada = new Factura();
        facturaPagada.setId(3L);
        facturaPagada.setEstudianteId(1L);
        facturaPagada.setNumeroFactura("FAC-0003");
        facturaPagada.setMontoOriginal(BigDecimal.valueOf(800));
        facturaPagada.setMontoPagado(BigDecimal.valueOf(800));
        facturaPagada.setSaldo(BigDecimal.ZERO);
        facturaPagada.setEstado("PAGADO");
        facturaPagada.setFechaVencimiento(LocalDate.now().minusDays(20));
        facturaPagada.setCreatedAt(ahora.minusDays(30));
        facturaPagada.setDeletedAt(null);

        facturaVencida = new Factura();
        facturaVencida.setId(4L);
        facturaVencida.setEstudianteId(1L);
        facturaVencida.setNumeroFactura("FAC-0004");
        facturaVencida.setMontoOriginal(BigDecimal.valueOf(1500));
        facturaVencida.setMontoPagado(BigDecimal.ZERO);
        facturaVencida.setSaldo(BigDecimal.valueOf(1500));
        facturaVencida.setEstado("PENDIENTE");
        facturaVencida.setFechaVencimiento(LocalDate.now().minusDays(5));
        facturaVencida.setCreatedAt(ahora.minusDays(40));
        facturaVencida.setDeletedAt(null);
    }

    @Test
    void testObtenerEstadoCuenta_WithMultipleInvoices() {
        List<Factura> facturas = List.of(facturaPendiente, facturaParcial, facturaPagada, facturaVencida);
        Page<Factura> page = new PageImpl<>(facturas);

        when(facturaRepository.findByEstudianteIdAndDeletedAtIsNull(1L, PageRequest.of(0, 1000)))
            .thenReturn(page);

        FacturaListResponse pendienteResponse = new FacturaListResponse(1L, "FAC-0001", 1L, BigDecimal.valueOf(1000), BigDecimal.ZERO, BigDecimal.valueOf(1000), "PENDIENTE", LocalDate.now(), "CONTADO", 1, 0, LocalDateTime.now());
        FacturaListResponse parcialResponse = new FacturaListResponse(2L, "FAC-0002", 1L, BigDecimal.valueOf(500), BigDecimal.valueOf(200), BigDecimal.valueOf(300), "PARCIAL", LocalDate.now(), "CONTADO", 1, 0, LocalDateTime.now());
        FacturaListResponse vencidaResponse = new FacturaListResponse(4L, "FAC-0004", 1L, BigDecimal.valueOf(1500), BigDecimal.ZERO, BigDecimal.valueOf(1500), "PENDIENTE", LocalDate.now(), "CONTADO", 1, 0, LocalDateTime.now());

        when(facturaMapper.toListResponse(facturaPendiente)).thenReturn(pendienteResponse);
        when(facturaMapper.toListResponse(facturaParcial)).thenReturn(parcialResponse);
        when(facturaMapper.toListResponse(facturaVencida)).thenReturn(vencidaResponse);

        EstadoCuentaResponse result = estadoCuentaService.obtenerEstadoCuenta(1L);

        assertNotNull(result);
        assertEquals(1L, result.estudianteId());

        assertEquals(BigDecimal.valueOf(1300), result.saldoPendiente());
        assertEquals(BigDecimal.ZERO, result.saldoPagado());
        assertEquals(BigDecimal.valueOf(1500), result.saldoVencido());

        assertEquals(2, result.cantidadFacturasPendientes());
        assertEquals(1, result.cantidadFacturasPagadas());
        assertEquals(1, result.cantidadFacturasVencidas());

        assertEquals(3, result.facturasActivas().size());

        verify(facturaRepository, times(1)).findByEstudianteIdAndDeletedAtIsNull(1L, PageRequest.of(0, 1000));
    }

    @Test
    void testObtenerEstadoCuenta_NoInvoices() {
        Page<Factura> emptyPage = new PageImpl<>(List.of());

        when(facturaRepository.findByEstudianteIdAndDeletedAtIsNull(999L, PageRequest.of(0, 1000)))
            .thenReturn(emptyPage);

        EstadoCuentaResponse result = estadoCuentaService.obtenerEstadoCuenta(999L);

        assertNotNull(result);
        assertEquals(999L, result.estudianteId());
        assertEquals(BigDecimal.ZERO, result.saldoPendiente());
        assertEquals(BigDecimal.ZERO, result.saldoPagado());
        assertEquals(BigDecimal.ZERO, result.saldoVencido());
        assertEquals(0, result.cantidadFacturasPendientes());
        assertEquals(0, result.cantidadFacturasPagadas());
        assertEquals(0, result.cantidadFacturasVencidas());
        assertNull(result.ultimaActividad());
        assertTrue(result.facturasActivas().isEmpty());

        verify(facturaRepository, times(1)).findByEstudianteIdAndDeletedAtIsNull(999L, PageRequest.of(0, 1000));
    }

    @Test
    void testObtenerEstadoCuenta_OnlyPaidInvoices() {
        List<Factura> facturas = List.of(facturaPagada);
        Page<Factura> page = new PageImpl<>(facturas);

        when(facturaRepository.findByEstudianteIdAndDeletedAtIsNull(1L, PageRequest.of(0, 1000)))
            .thenReturn(page);

        EstadoCuentaResponse result = estadoCuentaService.obtenerEstadoCuenta(1L);

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.saldoPendiente());
        assertEquals(BigDecimal.ZERO, result.saldoPagado());
        assertEquals(BigDecimal.ZERO, result.saldoVencido());
        assertEquals(0, result.cantidadFacturasPendientes());
        assertEquals(1, result.cantidadFacturasPagadas());
        assertEquals(0, result.cantidadFacturasVencidas());
        assertTrue(result.facturasActivas().isEmpty());

        verify(facturaRepository, times(1)).findByEstudianteIdAndDeletedAtIsNull(1L, PageRequest.of(0, 1000));
    }

    @Test
    void testObtenerEstadoCuenta_UltimaActividad() {
        List<Factura> facturas = List.of(facturaPendiente, facturaVencida);
        Page<Factura> page = new PageImpl<>(facturas);

        when(facturaRepository.findByEstudianteIdAndDeletedAtIsNull(1L, PageRequest.of(0, 1000)))
            .thenReturn(page);

        FacturaListResponse pendienteResponse = new FacturaListResponse(1L, "FAC-0001", 1L, BigDecimal.valueOf(1000), BigDecimal.ZERO, BigDecimal.valueOf(1000), "PENDIENTE", LocalDate.now(), "CONTADO", 1, 0, LocalDateTime.now());
        FacturaListResponse vencidaResponse = new FacturaListResponse(4L, "FAC-0004", 1L, BigDecimal.valueOf(1500), BigDecimal.ZERO, BigDecimal.valueOf(1500), "PENDIENTE", LocalDate.now(), "CONTADO", 1, 0, LocalDateTime.now());

        when(facturaMapper.toListResponse(facturaPendiente)).thenReturn(pendienteResponse);
        when(facturaMapper.toListResponse(facturaVencida)).thenReturn(vencidaResponse);

        EstadoCuentaResponse result = estadoCuentaService.obtenerEstadoCuenta(1L);

        assertNotNull(result);
        assertNotNull(result.ultimaActividad());
        assertEquals(facturaPendiente.getCreatedAt(), result.ultimaActividad());

        verify(facturaRepository, times(1)).findByEstudianteIdAndDeletedAtIsNull(1L, PageRequest.of(0, 1000));
    }

    @Test
    void testObtenerEstadoCuenta_FiltersDeletedInvoices() {
        Factura facturaEliminada = new Factura();
        facturaEliminada.setId(5L);
        facturaEliminada.setEstudianteId(1L);
        facturaEliminada.setDeletedAt(LocalDateTime.now());

        List<Factura> facturas = List.of(facturaPendiente);
        Page<Factura> page = new PageImpl<>(facturas);

        when(facturaRepository.findByEstudianteIdAndDeletedAtIsNull(1L, PageRequest.of(0, 1000)))
            .thenReturn(page);

        FacturaListResponse pendienteResponse = new FacturaListResponse(1L, "FAC-0001", 1L, BigDecimal.valueOf(1000), BigDecimal.ZERO, BigDecimal.valueOf(1000), "PENDIENTE", LocalDate.now(), "CONTADO", 1, 0, LocalDateTime.now());
        when(facturaMapper.toListResponse(facturaPendiente)).thenReturn(pendienteResponse);

        EstadoCuentaService service = estadoCuentaService;
        EstadoCuentaResponse result = service.obtenerEstadoCuenta(1L);

        assertNotNull(result);
        assertEquals(1, result.facturasActivas().size());

        verify(facturaRepository, times(1)).findByEstudianteIdAndDeletedAtIsNull(1L, PageRequest.of(0, 1000));
    }
}
