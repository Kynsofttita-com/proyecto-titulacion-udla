package com.escuela.cobros.service;

import com.escuela.cobros.dto.EstadoCuentaResponse;
import com.escuela.cobros.dto.FacturaListResponse;
import com.escuela.cobros.entity.Factura;
import com.escuela.cobros.mapper.EstadoCuentaMapper;
import com.escuela.cobros.mapper.FacturaMapper;
import com.escuela.cobros.repository.FacturaRepository;
import com.escuela.cobros.repository.PagoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class EstadoCuentaServiceImpl implements EstadoCuentaService {

    private final FacturaRepository facturaRepository;
    private final PagoRepository pagoRepository;
    private final EstadoCuentaMapper estadoCuentaMapper;
    private final FacturaMapper facturaMapper;

    @Override
    public EstadoCuentaResponse obtenerEstadoCuenta(Long estudianteId) {
        log.debug("Obteniendo estado de cuenta para estudiante: {}", estudianteId);

        Page<Factura> facturasPage = facturaRepository
            .findByEstudianteIdAndDeletedAtIsNull(estudianteId, PageRequest.of(0, 1000));
        List<Factura> facturas = facturasPage.getContent();

        if (facturas.isEmpty()) {
            log.debug("Estudiante {} sin facturas", estudianteId);
            return new EstadoCuentaResponse(
                estudianteId,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                0, 0, 0,
                null,
                List.of()
            );
        }

        BigDecimal saldoPendiente = BigDecimal.ZERO;
        BigDecimal saldoPagado = BigDecimal.ZERO;
        BigDecimal saldoVencido = BigDecimal.ZERO;
        int pendientes = 0;
        int pagadas = 0;
        int vencidas = 0;
        LocalDateTime ultimaActividad = null;

        for (Factura factura : facturas) {
            LocalDateTime fechaFactura = factura.getCreatedAt();
            if (ultimaActividad == null || fechaFactura.isAfter(ultimaActividad)) {
                ultimaActividad = fechaFactura;
            }

            String estado = factura.getEstado();

            if ("PAGADO".equalsIgnoreCase(estado)) {
                saldoPagado = saldoPagado.add(factura.getSaldo() == null ? BigDecimal.ZERO : factura.getSaldo());
                pagadas++;
            } else if ("PENDIENTE".equalsIgnoreCase(estado) || "PARCIAL".equalsIgnoreCase(estado)) {
                boolean vencida = LocalDate.now().isAfter(factura.getFechaVencimiento());

                if (vencida) {
                    saldoVencido = saldoVencido.add(factura.getSaldo() == null ? BigDecimal.ZERO : factura.getSaldo());
                    vencidas++;
                } else {
                    saldoPendiente = saldoPendiente.add(factura.getSaldo() == null ? BigDecimal.ZERO : factura.getSaldo());
                    pendientes++;
                }
            }
        }

        List<FacturaListResponse> facturasActivas = facturas.stream()
            .filter(f -> !"PAGADO".equalsIgnoreCase(f.getEstado()))
            .map(facturaMapper::toListResponse)
            .collect(Collectors.toList());

        return new EstadoCuentaResponse(
            estudianteId,
            saldoPendiente,
            saldoPagado,
            saldoVencido,
            pendientes,
            pagadas,
            vencidas,
            ultimaActividad,
            facturasActivas
        );
    }
}
