package com.escuela.cobros.service;

import com.escuela.cobros.client.AuthClient;
import com.escuela.cobros.dto.PagoListResponse;
import com.escuela.cobros.dto.PagoRequest;
import com.escuela.cobros.dto.PagoResponse;
import com.escuela.cobros.entity.CuentaContable;
import com.escuela.cobros.entity.Factura;
import com.escuela.cobros.entity.MovimientoContable;
import com.escuela.cobros.entity.Pago;
import com.escuela.cobros.exception.FacturaNotFoundException;
import com.escuela.cobros.exception.SaldoInsuficienteException;
import com.escuela.cobros.mapper.PagoMapper;
import com.escuela.cobros.repository.CuentaContableRepository;
import com.escuela.cobros.repository.FacturaCuotaRepository;
import com.escuela.cobros.repository.FacturaRepository;
import com.escuela.cobros.repository.PagoRepository;
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
class PagoServiceImplTest {

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private FacturaRepository facturaRepository;

    @Mock
    private FacturaCuotaRepository facturaCuotaRepository;

    @Mock
    private CuentaContableRepository cuentaContableRepository;

    @Mock
    private MovimientoContableService movimientoContableService;

    @Mock
    private PagoMapper pagoMapper;

    @Mock
    private PagoEventDispatcher eventDispatcher;

    @Mock
    private AuthClient authClient;

    @InjectMocks
    private PagoServiceImpl pagoService;

    private Factura facturaMock;
    private Pago pagoMock;
    private CuentaContable cuentaMock;
    private PagoRequest pagoRequest;
    private PagoResponse pagoResponse;

    @BeforeEach
    void setUp() {
        facturaMock = new Factura();
        facturaMock.setId(1L);
        facturaMock.setEstudianteId(1L);
        facturaMock.setMontoOriginal(BigDecimal.valueOf(1000));
        facturaMock.setMontoPagado(BigDecimal.ZERO);
        facturaMock.setSaldo(BigDecimal.valueOf(1000));
        facturaMock.setEstado("PENDIENTE");
        facturaMock.setFechaVencimiento(LocalDate.now().plusMonths(1));

        pagoMock = new Pago();
        pagoMock.setId(1L);
        pagoMock.setFactura(facturaMock);
        pagoMock.setMonto(BigDecimal.valueOf(500));
        pagoMock.setMetodoPago("EFECTIVO");
        pagoMock.setFechaPago(LocalDateTime.now());
        pagoMock.setCuentaId(1L);

        cuentaMock = new CuentaContable();
        cuentaMock.setId(1L);
        cuentaMock.setNombre("Caja");
        cuentaMock.setTipo("EFECTIVO");
        cuentaMock.setActivo(true);
        cuentaMock.setSaldoInicial(BigDecimal.ZERO);

        pagoRequest = new PagoRequest(
            1L,
            BigDecimal.valueOf(500),
            "EFECTIVO",
            1L,
            null,
            null
        );

        pagoResponse = new PagoResponse(
            1L,
            1L,
            BigDecimal.valueOf(500),
            LocalDateTime.now(),
            "EFECTIVO",
            1L,
            null,
            null,
            1L,
            null,
            null
        );
    }

    @Test
    void testFindById_Success() {
        when(pagoRepository.findById(1L))
            .thenReturn(Optional.of(pagoMock));
        when(pagoMapper.toResponse(pagoMock))
            .thenReturn(pagoResponse);

        PagoResponse result = pagoService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        verify(pagoRepository, times(1)).findById(1L);
    }

    @Test
    void testFindById_NotFound() {
        when(pagoRepository.findById(999L))
            .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> pagoService.findById(999L));
    }

    @Test
    void testCreate_Success() {
        when(facturaRepository.findByIdAndDeletedAtIsNull(1L))
            .thenReturn(Optional.of(facturaMock));
        when(cuentaContableRepository.findById(1L))
            .thenReturn(Optional.of(cuentaMock));
        when(pagoMapper.toEntity(pagoRequest))
            .thenReturn(pagoMock);
        when(pagoRepository.save(any(Pago.class)))
            .thenReturn(pagoMock);
        when(facturaRepository.save(any(Factura.class)))
            .thenReturn(facturaMock);
        when(movimientoContableService.crearDesdePago(any(Pago.class), any(CuentaContable.class)))
            .thenReturn(new MovimientoContable());
        when(pagoMapper.toResponse(pagoMock))
            .thenReturn(pagoResponse);

        PagoResponse result = pagoService.create(pagoRequest);

        assertNotNull(result);
        verify(facturaRepository, times(1)).findByIdAndDeletedAtIsNull(1L);
        verify(cuentaContableRepository, times(1)).findById(1L);
        verify(pagoRepository, times(1)).save(any(Pago.class));
        verify(facturaRepository, times(1)).save(any(Factura.class));
        verify(movimientoContableService, times(1)).crearDesdePago(any(Pago.class), any(CuentaContable.class));
        verify(eventDispatcher, times(1)).publishRegistrado(any(Pago.class), any(Factura.class));
    }

    @Test
    void testCreate_FacturaNoEncontrada() {
        when(facturaRepository.findByIdAndDeletedAtIsNull(999L))
            .thenReturn(Optional.empty());

        PagoRequest request = new PagoRequest(
            999L,
            BigDecimal.valueOf(500),
            "EFECTIVO",
            1L,
            null,
            null
        );

        assertThrows(FacturaNotFoundException.class, () -> pagoService.create(request));
    }

    @Test
    void testCreate_SaldoInsuficiente() {
        when(facturaRepository.findByIdAndDeletedAtIsNull(1L))
            .thenReturn(Optional.of(facturaMock));

        PagoRequest request = new PagoRequest(
            1L,
            BigDecimal.valueOf(2000),
            "EFECTIVO",
            1L,
            null,
            null
        );

        assertThrows(SaldoInsuficienteException.class, () -> pagoService.create(request));
    }

    @Test
    void testCreate_FacturaCancelada() {
        facturaMock.setEstado("CANCELADA");
        when(facturaRepository.findByIdAndDeletedAtIsNull(1L))
            .thenReturn(Optional.of(facturaMock));

        assertThrows(RuntimeException.class, () -> pagoService.create(pagoRequest));
    }

    @Test
    void testFindAll() {
        Pageable pageable = PageRequest.of(0, 50);
        Page<Pago> page = new PageImpl<>(List.of(pagoMock));

        when(pagoRepository.findAll(pageable))
            .thenReturn(page);
        when(pagoMapper.toListResponse(pagoMock))
            .thenReturn(new PagoListResponse(1L, 1L, BigDecimal.valueOf(500),
                LocalDateTime.now(), "EFECTIVO", null));

        Page<PagoListResponse> result = pagoService.findAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(pagoRepository, times(1)).findAll(pageable);
    }

    @Test
    void testFindByEstudianteId() {
        Pageable pageable = PageRequest.of(0, 50);
        Page<Pago> page = new PageImpl<>(List.of(pagoMock));

        when(pagoRepository.findByEstudianteId(1L, pageable))
            .thenReturn(page);
        when(pagoMapper.toListResponse(pagoMock))
            .thenReturn(new PagoListResponse(1L, 1L, BigDecimal.valueOf(500),
                LocalDateTime.now(), "EFECTIVO", null));

        Page<PagoListResponse> result = pagoService.findByEstudianteId(1L, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(pagoRepository, times(1)).findByEstudianteId(1L, pageable);
    }

    @Test
    void testFindByFacturaId() {
        Pageable pageable = PageRequest.of(0, 50);
        Page<Pago> page = new PageImpl<>(List.of(pagoMock));

        when(pagoRepository.findByFacturaId(1L, pageable))
            .thenReturn(page);
        when(pagoMapper.toListResponse(pagoMock))
            .thenReturn(new PagoListResponse(1L, 1L, BigDecimal.valueOf(500),
                LocalDateTime.now(), "EFECTIVO", null));

        Page<PagoListResponse> result = pagoService.findByFacturaId(1L, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(pagoRepository, times(1)).findByFacturaId(1L, pageable);
    }
}
