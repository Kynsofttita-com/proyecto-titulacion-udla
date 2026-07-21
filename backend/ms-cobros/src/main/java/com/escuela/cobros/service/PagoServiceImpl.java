package com.escuela.cobros.service;

import com.escuela.cobros.dto.PagoListResponse;
import com.escuela.cobros.dto.PagoRequest;
import com.escuela.cobros.dto.PagoResponse;
import com.escuela.cobros.entity.CuentaContable;
import com.escuela.cobros.entity.Factura;
import com.escuela.cobros.entity.FacturaCuota;
import com.escuela.cobros.entity.Pago;
import com.escuela.cobros.exception.FacturaNotFoundException;
import com.escuela.cobros.exception.PagoNotFoundException;
import com.escuela.cobros.exception.SaldoInsuficienteException;
import com.escuela.cobros.mapper.PagoMapper;
import com.escuela.cobros.repository.CuentaContableRepository;
import com.escuela.cobros.repository.FacturaCuotaRepository;
import com.escuela.cobros.repository.FacturaRepository;
import com.escuela.cobros.repository.PagoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class PagoServiceImpl implements PagoService {

    private static final List<String> CUOTA_PENDIENTE = List.of("PENDIENTE", "PARCIAL");

    private final PagoRepository pagoRepository;
    private final FacturaRepository facturaRepository;
    private final FacturaCuotaRepository facturaCuotaRepository;
    private final CuentaContableRepository cuentaContableRepository;
    private final MovimientoContableService movimientoContableService;
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
            .orElseThrow(() -> new PagoNotFoundException(id));
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
        log.info("Registrando pago para factura: {} cuenta: {}", request.facturaId(), request.cuentaId());

        Factura factura = facturaRepository.findByIdAndDeletedAtIsNull(request.facturaId())
            .orElseThrow(() -> new FacturaNotFoundException(request.facturaId()));

        validarFacturaEstadoValido(factura);
        validarSaldoSuficiente(request.monto(), factura);

        // Validar cuenta contable (activa)
        CuentaContable cuenta = cuentaContableRepository.findById(request.cuentaId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Cuenta contable no encontrada: " + request.cuentaId()));
        if (Boolean.FALSE.equals(cuenta.getActivo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La cuenta '" + cuenta.getNombre() + "' esta desactivada");
        }

        Pago pago = pagoMapper.toEntity(request);
        pago.setFactura(factura);
        pago.setFechaPago(LocalDateTime.now());
        pago.setCuentaId(cuenta.getId());
        if (pago.getUsuarioRegistroId() == null) {
            Long uid = getUsuarioActualId();
            pago.setUsuarioRegistroId(uid != null ? uid : 1L);
        }

        // Si la factura es CREDITO, aplicamos el pago a la cuota más antigua
        // pendiente y vinculamos el pago a esa cuota.
        if ("CREDITO".equals(factura.getTipoPago())) {
            aplicarPagoACuota(pago, factura);
        }

        Pago pagGuardado = pagoRepository.save(pago);
        log.debug("Pago guardado con ID: {}", pagGuardado.getId());

        actualizarFacturaYEstado(factura, request.monto());

        // Generar el movimiento contable INGRESO asociado (misma transaccion)
        movimientoContableService.crearDesdePago(pagGuardado, cuenta);

        eventDispatcher.publishRegistrado(pagGuardado, factura);
        log.info("Pago registrado con ID: {} para factura: {} cuenta: {}",
                pagGuardado.getId(), factura.getId(), cuenta.getNombre());

        return pagoMapper.toResponse(pagGuardado);
    }

    /**
     * Aplica el monto del pago a la cuota más antigua que aún tiene saldo.
     * Si el monto del pago supera el saldo de la cuota, solo cubre esa cuota
     * (no se reparte automáticamente entre varias). El operador debe registrar
     * un segundo pago si quiere abonar a la siguiente cuota.
     */
    private void aplicarPagoACuota(Pago pago, Factura factura) {
        Optional<FacturaCuota> cuotaOpt = facturaCuotaRepository
            .findFirstByFacturaIdAndEstadoInOrderByNumeroCuotaAsc(factura.getId(), CUOTA_PENDIENTE);

        if (cuotaOpt.isEmpty()) {
            log.warn("Factura {} es CREDITO pero no tiene cuotas pendientes — pago se registra suelto",
                     factura.getId());
            return;
        }

        FacturaCuota cuota = cuotaOpt.get();
        BigDecimal saldoCuota = cuota.getMonto().subtract(cuota.getMontoPagado());

        if (pago.getMonto().compareTo(saldoCuota) > 0) {
            throw new IllegalArgumentException(String.format(
                "El monto del pago (%s) excede el saldo de la cuota %d (%s). " +
                "Registre un pago por el saldo exacto de la cuota o ajuste el monto.",
                pago.getMonto(), cuota.getNumeroCuota(), saldoCuota));
        }

        BigDecimal nuevoPagado = cuota.getMontoPagado().add(pago.getMonto());
        cuota.setMontoPagado(nuevoPagado);

        if (nuevoPagado.compareTo(cuota.getMonto()) >= 0) {
            cuota.setEstado("PAGADA");
            cuota.setFechaPagoCompleta(LocalDateTime.now());
            factura.setCuotasPagadas(factura.getCuotasPagadas() + 1);
            log.debug("Cuota {} de factura {} saldada", cuota.getNumeroCuota(), factura.getId());
        } else {
            cuota.setEstado("PARCIAL");
        }

        facturaCuotaRepository.save(cuota);

        pago.setNumeroCuota(cuota.getNumeroCuota());
        pago.setFacturaCuotaId(cuota.getId());
    }

    private void validarFacturaEstadoValido(Factura factura) {
        if ("CANCELADA".equalsIgnoreCase(factura.getEstado())
            || "ANULADA".equalsIgnoreCase(factura.getEstado())) {
            log.warn("Factura {} está anulada, no se puede registrar pago", factura.getId());
            throw new IllegalArgumentException("Factura está anulada, no se puede registrar pago");
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
            factura.setEstado("PAGADA");
            log.debug("Factura {} completamente pagada", factura.getId());
        } else if (nuevoMontoPagado.compareTo(BigDecimal.ZERO) > 0) {
            factura.setEstado("PARCIAL");
            log.debug("Factura {} parcialmente pagada", factura.getId());
        }

        facturaRepository.save(factura);
    }
}
