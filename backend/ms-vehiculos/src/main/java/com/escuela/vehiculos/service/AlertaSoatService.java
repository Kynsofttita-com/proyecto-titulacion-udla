package com.escuela.vehiculos.service;

import com.escuela.vehiculos.dto.AlertaDocumentoResponse;
import com.escuela.vehiculos.dto.AlertaSoatResponse;
import com.escuela.vehiculos.entity.Vehiculo;
import com.escuela.vehiculos.repository.VehiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
public class AlertaSoatService {

    private final VehiculoRepository vehiculoRepository;

    public AlertaSoatService(VehiculoRepository vehiculoRepository) {
        this.vehiculoRepository = vehiculoRepository;
    }

    /**
     * Lista vehiculos con SOAT vencido o por vencer en {@code dias} dias (inclusive).
     * Ordenado por fecha de vencimiento ascendente (mas urgente primero).
     * Se mantiene para compatibilidad con clientes existentes.
     */
    @Transactional(readOnly = true)
    public List<AlertaSoatResponse> alertasSoat(int dias) {
        LocalDate hoy = LocalDate.now();
        LocalDate limite = hoy.plusDays(dias);
        return vehiculoRepository.findConSoatPorVencer(limite).stream()
                .map(v -> {
                    long diasFalta = ChronoUnit.DAYS.between(hoy, v.getSoatVencimiento());
                    // Un SOAT que vence hoy (diasFalta == 0) ya se considera vencido:
                    // desde el dia del vencimiento inclusive no debe circular el vehiculo.
                    // Mismo criterio que morosidad (vence hoy y no se pago = ya moroso).
                    boolean vencido = diasFalta <= 0;
                    return new AlertaSoatResponse(
                            v.getId(), v.getPlaca(), v.getMarca(), v.getModelo(),
                            v.getSoatVencimiento(), diasFalta, vencido);
                })
                .toList();
    }

    /**
     * Alertas unificadas de SOAT + RTV (revision tecnica). Devuelve una entrada
     * por documento; un vehiculo puede aparecer 2 veces si tiene ambos por
     * vencer. Ordenado por urgencia (fecha ascendente).
     */
    @Transactional(readOnly = true)
    public List<AlertaDocumentoResponse> alertasDocumentos(int dias) {
        LocalDate hoy = LocalDate.now();
        LocalDate limite = hoy.plusDays(dias);

        Stream<AlertaDocumentoResponse> soat = vehiculoRepository.findConSoatPorVencer(limite).stream()
                .map(v -> construir(v, "SOAT", v.getSoatVencimiento(), hoy));

        Stream<AlertaDocumentoResponse> rtv = vehiculoRepository.findConRevisionPorVencer(limite).stream()
                .map(v -> construir(v, "RTV", v.getRevisionVencimiento(), hoy));

        return Stream.concat(soat, rtv)
                .sorted(Comparator.comparing(AlertaDocumentoResponse::fechaVencimiento))
                .toList();
    }

    private AlertaDocumentoResponse construir(Vehiculo v, String tipo, LocalDate vencimiento, LocalDate hoy) {
        long diasFalta = ChronoUnit.DAYS.between(hoy, vencimiento);
        boolean vencido = diasFalta <= 0;
        return new AlertaDocumentoResponse(
                v.getId(), v.getPlaca(), v.getMarca(), v.getModelo(),
                tipo, vencimiento, diasFalta, vencido);
    }
}
