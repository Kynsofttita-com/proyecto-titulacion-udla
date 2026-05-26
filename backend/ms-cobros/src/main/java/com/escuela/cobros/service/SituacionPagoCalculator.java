package com.escuela.cobros.service;

import com.escuela.cobros.entity.Factura;
import com.escuela.cobros.repository.FacturaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Calcula la {@code situacion_pago} actual de un estudiante leyendo sus
 * facturas.
 *
 * <p><b>Modelo simplificado (Sprint 9 ext, 2026-05-24):</b> el sistema asume
 * que el cobro a CREDITO se hace por debito automatico de tarjeta, por lo
 * que una factura CREDITO emitida ya cuenta como {@code PAGADO_TOTAL} desde
 * el momento de la emision (no existe el concepto de "mora").</p>
 *
 * <p>Reglas (en orden de evaluacion):
 * <ol>
 *   <li>Sin facturas activas → {@code PENDIENTE_FACTURACION}</li>
 *   <li>Alguna factura CONTADO con saldo &gt; 0:
 *     <ul>
 *       <li>Si el saldo es igual al monto original (0% pagado) →
 *           {@code PENDIENTE_PAGO}</li>
 *       <li>Si el saldo es intermedio →
 *           {@code PAGO_PARCIAL}</li>
 *     </ul>
 *   </li>
 *   <li>Todas las facturas saldadas <i>o</i> CREDITO (asume pagado por
 *       debito automatico) → {@code PAGADO_TOTAL}</li>
 * </ol>
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class SituacionPagoCalculator {

    private final FacturaRepository facturaRepository;

    public String calcular(Long estudianteId) {
        List<Factura> facturas = facturaRepository
            .findByEstudianteIdAndDeletedAtIsNull(estudianteId,
                org.springframework.data.domain.PageRequest.of(0, 1000))
            .getContent()
            .stream()
            .filter(f -> !"ANULADA".equalsIgnoreCase(f.getEstado())
                      && !"CANCELADA".equalsIgnoreCase(f.getEstado()))
            .toList();

        if (facturas.isEmpty()) {
            return "PENDIENTE_FACTURACION";
        }

        boolean haySinPago = false;
        boolean hayParcial = false;

        for (Factura f : facturas) {
            // CREDITO emitida = se considera PAGADO_TOTAL (debito automatico
            // asumido). No contribuye a saldo desde la perspectiva del
            // estudiante; el cobro de cuotas corre en background.
            if ("CREDITO".equals(f.getTipoPago())) {
                continue;
            }

            BigDecimal saldo = f.getMontoOriginal().subtract(f.getMontoPagado());
            int cmpSaldo = saldo.compareTo(BigDecimal.ZERO);
            if (cmpSaldo <= 0) {
                continue; // CONTADO saldada
            }

            // CONTADO con saldo: distinguir 0% pagado vs parcial
            if (f.getMontoPagado().compareTo(BigDecimal.ZERO) == 0) {
                haySinPago = true;
            } else {
                hayParcial = true;
            }
        }

        if (hayParcial)  return "PAGO_PARCIAL";
        if (haySinPago)  return "PENDIENTE_PAGO";
        return "PAGADO_TOTAL";
    }
}
