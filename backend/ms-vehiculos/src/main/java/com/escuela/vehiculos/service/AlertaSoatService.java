package com.escuela.vehiculos.service;

import com.escuela.vehiculos.dto.AlertaSoatResponse;
import com.escuela.vehiculos.repository.VehiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class AlertaSoatService {

    private final VehiculoRepository vehiculoRepository;

    public AlertaSoatService(VehiculoRepository vehiculoRepository) {
        this.vehiculoRepository = vehiculoRepository;
    }

    /**
     * Lista vehiculos con SOAT vencido o por vencer en {@code dias} dias (inclusive).
     * Ordenado por fecha de vencimiento ascendente (mas urgente primero).
     */
    @Transactional(readOnly = true)
    public List<AlertaSoatResponse> alertasSoat(int dias) {
        LocalDate hoy = LocalDate.now();
        LocalDate limite = hoy.plusDays(dias);
        return vehiculoRepository.findConSoatPorVencer(limite).stream()
                .map(v -> {
                    long diasFalta = ChronoUnit.DAYS.between(hoy, v.getSoatVencimiento());
                    boolean vencido = diasFalta < 0;
                    return new AlertaSoatResponse(
                            v.getId(), v.getPlaca(), v.getMarca(), v.getModelo(),
                            v.getSoatVencimiento(), diasFalta, vencido);
                })
                .toList();
    }
}
