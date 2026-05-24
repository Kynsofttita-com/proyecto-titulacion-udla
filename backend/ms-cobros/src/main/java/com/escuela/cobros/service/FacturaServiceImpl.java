package com.escuela.cobros.service;

import com.escuela.cobros.client.EstudianteClient;
import com.escuela.cobros.dto.EstudianteDetailDTO;
import com.escuela.cobros.dto.FacturaCuotaResponse;
import com.escuela.cobros.dto.FacturaListResponse;
import com.escuela.cobros.dto.FacturaRequest;
import com.escuela.cobros.dto.FacturaResponse;
import com.escuela.cobros.entity.Factura;
import com.escuela.cobros.entity.FacturaCuota;
import com.escuela.cobros.exception.EstudianteInactivoException;
import com.escuela.cobros.exception.EstudianteNotFoundException;
import com.escuela.cobros.exception.FacturaNotFoundException;
import com.escuela.cobros.mapper.FacturaCuotaMapper;
import com.escuela.cobros.mapper.FacturaMapper;
import com.escuela.cobros.repository.FacturaCuotaRepository;
import com.escuela.cobros.repository.FacturaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class FacturaServiceImpl implements FacturaService {

    private final FacturaRepository facturaRepository;
    private final FacturaCuotaRepository facturaCuotaRepository;
    private final FacturaMapper facturaMapper;
    private final FacturaCuotaMapper facturaCuotaMapper;
    private final EstudianteClient estudianteClient;
    private final FacturaEventDispatcher eventDispatcher;

    @Override
    @Transactional(readOnly = true)
    public Page<FacturaListResponse> findAll(Pageable pageable) {
        log.debug("Buscando todas las facturas con paginación: {}", pageable);
        return facturaRepository.findByDeletedAtIsNull(pageable)
            .map(facturaMapper::toListResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public FacturaResponse findById(Long id) {
        log.debug("Buscando factura con ID: {}", id);
        Factura factura = facturaRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new FacturaNotFoundException(id));
        return facturaMapper.toResponse(factura);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FacturaListResponse> findByEstudianteId(Long estudianteId, Pageable pageable) {
        log.debug("Buscando facturas del estudiante: {} con paginación: {}", estudianteId, pageable);
        return facturaRepository.findByEstudianteIdAndDeletedAtIsNull(estudianteId, pageable)
            .map(facturaMapper::toListResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FacturaCuotaResponse> findCuotas(Long facturaId) {
        log.debug("Buscando cuotas de factura: {}", facturaId);
        // valida que la factura existe
        facturaRepository.findByIdAndDeletedAtIsNull(facturaId)
            .orElseThrow(() -> new FacturaNotFoundException(facturaId));
        return facturaCuotaRepository.findByFacturaIdOrderByNumeroCuotaAsc(facturaId)
            .stream()
            .map(facturaCuotaMapper::toResponse)
            .toList();
    }

    @Override
    public FacturaResponse create(FacturaRequest request) {
        log.info("Creando factura para estudiante: {} (tipoPago={}, cuotas={})",
                 request.estudianteId(), request.tipoPago(), request.numeroCuotas());

        validarEstudianteExisteYActivo(request.estudianteId());
        validarConsistenciaCredito(request);

        String numeroFactura = generarNumeroFactura();

        Factura factura = facturaMapper.toEntity(request);
        factura.setNumeroFactura(numeroFactura);
        factura.setEstado("PENDIENTE");
        factura.setMontoPagado(BigDecimal.ZERO);
        factura.setFechaEmision(LocalDate.now());

        // Crédito: persistir cuotas_pagadas=0 y valor_cuota calculado
        factura.setCuotasPagadas(0);
        factura.setValorCuota(calcularValorCuota(factura.getMontoOriginal(), factura.getNumeroCuotas()));

        Factura guardada = facturaRepository.save(factura);

        // Si es CREDITO genera las filas de factura_cuotas
        if ("CREDITO".equals(guardada.getTipoPago())) {
            generarCuotas(guardada);
        }

        log.info("Factura creada con ID: {} y número: {}", guardada.getId(), numeroFactura);
        return facturaMapper.toResponse(guardada);
    }

    @Override
    public FacturaResponse update(Long id, FacturaRequest request) {
        log.info("Actualizando factura con ID: {}", id);

        Factura factura = facturaRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new FacturaNotFoundException(id));

        // Bloquear edición de campos de crédito si ya tiene pagos aplicados.
        if (factura.getCuotasPagadas() != null && factura.getCuotasPagadas() > 0) {
            throw new IllegalStateException(
                "No se puede modificar una factura con cuotas pagadas. Anule y emita una nueva.");
        }

        validarEstudianteExisteYActivo(request.estudianteId());

        facturaMapper.updateEntity(request, factura);
        Factura actualizada = facturaRepository.save(factura);

        log.info("Factura actualizada con ID: {}", id);
        return facturaMapper.toResponse(actualizada);
    }

    @Override
    public void softDelete(Long id) {
        log.info("Soft-deletando factura con ID: {}", id);

        Factura factura = facturaRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new FacturaNotFoundException(id));

        factura.setDeletedAt(LocalDateTime.now());
        facturaRepository.save(factura);

        eventDispatcher.publishCancelada(factura);
        log.info("Factura soft-deletada con ID: {}", id);
    }

    // =====================================================================
    // HELPERS
    // =====================================================================

    private void validarConsistenciaCredito(FacturaRequest req) {
        if ("CREDITO".equals(req.tipoPago())) {
            if (req.numeroCuotas() == null || req.numeroCuotas() < 2) {
                throw new IllegalArgumentException(
                    "Una factura a CREDITO requiere al menos 2 cuotas");
            }
            if (req.frecuenciaCuota() == null) {
                throw new IllegalArgumentException(
                    "Una factura a CREDITO requiere frecuenciaCuota (MENSUAL/QUINCENAL/SEMANAL)");
            }
            if (req.fechaPrimeraCuota() == null) {
                throw new IllegalArgumentException(
                    "Una factura a CREDITO requiere fechaPrimeraCuota");
            }
        } else {
            // CONTADO: forzamos numero_cuotas=1 sin frecuencia
            if (req.numeroCuotas() != null && req.numeroCuotas() != 1) {
                throw new IllegalArgumentException(
                    "Una factura CONTADO debe tener numeroCuotas=1");
            }
        }
    }

    private BigDecimal calcularValorCuota(BigDecimal montoTotal, Integer numCuotas) {
        if (numCuotas == null || numCuotas <= 1) return montoTotal;
        return montoTotal.divide(BigDecimal.valueOf(numCuotas), 2, RoundingMode.HALF_UP);
    }

    /**
     * Genera las {@code numero_cuotas} filas en {@code factura_cuotas} con
     * vencimientos calculados a partir de {@code fecha_primera_cuota} y
     * {@code frecuencia_cuota}. El residuo de la división se suma a la última
     * cuota para que la suma sea exactamente igual al monto original.
     */
    private void generarCuotas(Factura factura) {
        int n = factura.getNumeroCuotas();
        BigDecimal valorBase = factura.getValorCuota();
        BigDecimal total = factura.getMontoOriginal();
        // residuo = total - (valorBase * n). Se suma a la última cuota.
        BigDecimal residuo = total.subtract(valorBase.multiply(BigDecimal.valueOf(n)));

        List<FacturaCuota> cuotas = new ArrayList<>(n);
        for (int i = 1; i <= n; i++) {
            BigDecimal monto = (i == n) ? valorBase.add(residuo) : valorBase;
            LocalDate venc = calcularVencimientoCuota(
                factura.getFechaPrimeraCuota(), factura.getFrecuenciaCuota(), i - 1);

            cuotas.add(FacturaCuota.builder()
                .facturaId(factura.getId())
                .numeroCuota(i)
                .monto(monto)
                .montoPagado(BigDecimal.ZERO)
                .fechaVencimiento(venc)
                .estado("PENDIENTE")
                .build());
        }
        facturaCuotaRepository.saveAll(cuotas);
        log.info("Generadas {} cuotas para factura {}", n, factura.getId());
    }

    private LocalDate calcularVencimientoCuota(LocalDate primeraCuota, String frecuencia, int offset) {
        return switch (frecuencia) {
            case "MENSUAL"   -> primeraCuota.plusMonths(offset);
            case "QUINCENAL" -> primeraCuota.plusDays(15L * offset);
            case "SEMANAL"   -> primeraCuota.plusWeeks(offset);
            default          -> primeraCuota.plusMonths(offset);
        };
    }

    private void validarEstudianteExisteYActivo(Long estudianteId) {
        try {
            EstudianteDetailDTO estudiante = estudianteClient.obtenerEstudiante(estudianteId);

            // Estados que SÍ pueden facturar: PRE_MATRICULADO, MATRICULADO, CURSANDO.
            // Estados que NO: COMPLETADO (ya terminó), RETIRADO (abandonó) — y el
            // legado ACTIVO durante migración (se trata como MATRICULADO).
            String estado = estudiante.estado();
            if ("RETIRADO".equalsIgnoreCase(estado) || "INACTIVO".equalsIgnoreCase(estado)) {
                log.warn("Estudiante {} en estado {} no puede facturar", estudianteId, estado);
                throw new EstudianteInactivoException(estudianteId);
            }
        } catch (Exception e) {
            if (e instanceof EstudianteInactivoException) {
                throw e;
            }
            log.warn("No se pudo obtener estudiante {} via Feign", estudianteId, e);
            throw new EstudianteNotFoundException(estudianteId);
        }
    }

    private String generarNumeroFactura() {
        Long maxNumber = facturaRepository.findMaxNumeroFactura();
        long newNumber = (maxNumber != null ? maxNumber : 0) + 1;
        return String.format("FAC-%04d", newNumber);
    }
}
