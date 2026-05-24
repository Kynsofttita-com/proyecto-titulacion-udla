package com.escuela.cobros.service;

import com.escuela.cobros.entity.Factura;
import com.escuela.cobros.entity.FacturaCuota;
import com.escuela.cobros.repository.FacturaCuotaRepository;
import com.escuela.cobros.repository.FacturaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Calcula la {@code situacion_pago} actual de un estudiante leyendo sus
 * facturas y cuotas. Pensado para ser invocado desde MS-Estudiantes via
 * un endpoint REST cuando hace falta sincronizar (los eventos
 * {@code pago.registrado} mantienen el campo en vivo, pero pueden perderse
 * si MS-Estudiantes esta caido o la cola aun no existia).
 *
 * <p>Reglas:
 * <ul>
 *   <li>Sin facturas activas → {@code SIN_DEUDA}</li>
 *   <li>Alguna cuota vencida con saldo > 0 → {@code EN_MORA}</li>
 *   <li>Todas las facturas con saldo = 0 → {@code PAGADO_TOTAL}</li>
 *   <li>Tiene facturas a credito sin cuotas vencidas, con cuotas futuras pendientes → {@code AL_DIA}</li>
 *   <li>Resto (factura contado con saldo, o credito con saldo pero sin distincion al dia/mora) → {@code PAGO_PARCIAL}</li>
 * </ul>
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class SituacionPagoCalculator {

    private final FacturaRepository facturaRepository;
    private final FacturaCuotaRepository facturaCuotaRepository;

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
            return "SIN_DEUDA";
        }

        LocalDate hoy = LocalDate.now();
        boolean todasPagadas = true;
        boolean hayVencido = false;            // factura CONTADO o cuota CREDITO vencida con saldo
        boolean hayContadoConSaldo = false;    // factura CONTADO con saldo (vencida o no)
        boolean hayCreditoConSaldo = false;    // factura CREDITO con saldo

        for (Factura f : facturas) {
            BigDecimal saldo = f.getMontoOriginal().subtract(f.getMontoPagado());
            if (saldo.compareTo(BigDecimal.ZERO) <= 0) {
                continue; // factura saldada, no contribuye
            }
            todasPagadas = false;

            boolean esCredito = "CREDITO".equals(f.getTipoPago());
            if (esCredito) {
                hayCreditoConSaldo = true;
                // Cuotas vencidas con saldo → mora
                List<FacturaCuota> cuotas = facturaCuotaRepository.findByFacturaIdOrderByNumeroCuotaAsc(f.getId());
                for (FacturaCuota c : cuotas) {
                    BigDecimal saldoCuota = c.getMonto().subtract(c.getMontoPagado());
                    if (saldoCuota.compareTo(BigDecimal.ZERO) > 0
                        && c.getFechaVencimiento() != null
                        && c.getFechaVencimiento().isBefore(hoy)) {
                        hayVencido = true;
                    }
                }
            } else {
                hayContadoConSaldo = true;
                // Factura CONTADO vencida sin pagar = mora
                if (f.getFechaVencimiento() != null && f.getFechaVencimiento().isBefore(hoy)) {
                    hayVencido = true;
                }
            }
        }

        if (todasPagadas)        return "PAGADO_TOTAL";
        if (hayVencido)          return "EN_MORA";
        // Aqui: hay saldo pero nada vencido.
        // AL_DIA solo si la deuda es 100% credito sin vencer.
        if (hayCreditoConSaldo && !hayContadoConSaldo) return "AL_DIA";
        return "PAGO_PARCIAL";
    }
}
