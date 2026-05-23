package com.escuela.cobros.service;

import com.escuela.cobros.dto.PagoListResponse;
import com.escuela.cobros.dto.PagoRequest;
import com.escuela.cobros.dto.PagoResponse;
import com.escuela.cobros.entity.Factura;
import com.escuela.cobros.entity.Pago;
import com.escuela.cobros.exception.FacturaNotFoundException;
import com.escuela.cobros.exception.SaldoInsuficienteException;
import com.escuela.cobros.mapper.PagoMapper;
import com.escuela.cobros.repository.FacturaRepository;
import com.escuela.cobros.repository.PagoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class PagoServiceImpl implements PagoService {

    private final PagoRepository pagoRepository;
    private final FacturaRepository facturaRepository;
    private final PagoMapper pagoMapper;
    private final PagoEventDispatcher eventDispatcher;

    @Override
    public Page<PagoListResponse> findAll(Pageable pageable) {
        log.debug("Buscando todos los pagos con paginación: {}", pageable);
        return pagoRepository.findAll(pageable)
            .map(pagoMapper::toListResponse);
    }

    @Override
    public PagoResponse findById(Long id) {
        log.debug("Buscando pago con ID: {}", id);
        Pago pago = pagoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Pago con ID " + id + " no encontrado"));
        return pagoMapper.toResponse(pago);
    }

    @Override
    public Page<PagoListResponse> findByEstudianteId(Long estudianteId, Pageable pageable) {
        log.debug("Buscando pagos del estudiante: {} con paginación: {}", estudianteId, pageable);
        return pagoRepository.findByEstudianteId(estudianteId, pageable)
            .map(pagoMapper::toListResponse);
    }

    @Override
    public Page<PagoListResponse> findByFacturaId(Long facturaId, Pageable pageable) {
        log.debug("Buscando pagos de la factura: {} con paginación: {}", facturaId, pageable);
        return pagoRepository.findByFacturaId(facturaId, pageable)
            .map(pagoMapper::toListResponse);
    }

    @Override
    @Transactional
    public PagoResponse create(PagoRequest request) {
        log.info("Registrando pago para factura: {}", request.facturaId());

        Factura factura = facturaRepository.findByIdAndDeletedAtIsNull(request.facturaId())
            .orElseThrow(() -> new FacturaNotFoundException(request.facturaId()));

        validarFacturaEstadoValido(factura);
        validarSaldoSuficiente(request.monto(), factura);

        Pago pago = pagoMapper.toEntity(request);
        pago.setFactura(factura);
        pago.setFechaPago(java.time.LocalDateTime.now());
        if (pago.getUsuarioRegistroId() == null) {
            Long uid = getUsuarioActualId();
            pago.setUsuarioRegistroId(uid != null ? uid : 1L);
        }

        Pago pagGuardado = pagoRepository.save(pago);
        log.debug("Pago guardado con ID: {}", pagGuardado.getId());

        actualizarFacturaYEstado(factura, request.monto());

        eventDispatcher.publishRegistrado(pagGuardado, factura);
        log.info("Pago registrado con ID: {} para factura: {}", pagGuardado.getId(), factura.getId());

        return pagoMapper.toResponse(pagGuardado);
    }

    private void validarFacturaEstadoValido(Factura factura) {
        if ("CANCELADA".equalsIgnoreCase(factura.getEstado())) {
            log.warn("Factura {} está cancelada, no se puede registrar pago", factura.getId());
            throw new RuntimeException("Factura está cancelada, no se puede registrar pago");
        }
    }

    private void validarSaldoSuficiente(BigDecimal monto, Factura factura) {
        if (monto.compareTo(factura.getSaldo()) > 0) {
            log.warn("Monto de pago {} excede saldo disponible {} para factura {}",
                monto, factura.getSaldo(), factura.getId());
            throw new SaldoInsuficienteException(monto, factura.getSaldo());
        }
    }

    private Long getUsuarioActualId() {
        try {
            var attrs = (org.springframework.web.context.request.ServletRequestAttributes)
                    org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                String uid = attrs.getRequest().getHeader("X-User-Id");
                if (uid != null && !uid.isBlank()) return Long.parseLong(uid);
            }
        } catch (Exception ignored) {}
        return null;
    }

    private void actualizarFacturaYEstado(Factura factura, BigDecimal montoPago) {
        BigDecimal nuevoMontoPagado = factura.getMontoPagado().add(montoPago);
        factura.setMontoPagado(nuevoMontoPagado);

        if (nuevoMontoPagado.compareTo(factura.getMontoOriginal()) >= 0) {
            factura.setEstado("PAGADO");
            log.debug("Factura {} completamente pagada", factura.getId());
        } else if (nuevoMontoPagado.compareTo(BigDecimal.ZERO) > 0) {
            factura.setEstado("PARCIAL");
            log.debug("Factura {} parcialmente pagada", factura.getId());
        }

        facturaRepository.save(factura);
    }
}
