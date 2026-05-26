package com.escuela.instructores.service;

import com.escuela.instructores.dto.ResumenHorasResponse;
import com.escuela.instructores.entity.Instructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Calcula el resumen de horas trabajadas por un instructor en un rango de
 * fechas. Las horas cumplidas reales se obtendran de ms-asignaciones via
 * llamada HTTP cuando se exponga el endpoint correspondiente (TODO Sprint 11).
 * Mientras tanto se devuelve 0.
 */
@Service
@Transactional(readOnly = true)
public class ResumenHorasService {

    private static final Logger log = LoggerFactory.getLogger(ResumenHorasService.class);

    private final InstructorService instructorService;

    public ResumenHorasService(InstructorService instructorService) {
        this.instructorService = instructorService;
    }

    public ResumenHorasResponse calcular(Long instructorId, LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("Parametros 'desde' y 'hasta' son requeridos");
        }
        if (hasta.isBefore(desde)) {
            throw new IllegalArgumentException("'hasta' no puede ser anterior a 'desde'");
        }

        Instructor i = instructorService.buscarOFallar(instructorId);

        long dias = ChronoUnit.DAYS.between(desde, hasta) + 1; // inclusivo
        BigDecimal semanas = BigDecimal.valueOf(dias).divide(BigDecimal.valueOf(7), 4, RoundingMode.HALF_UP);
        BigDecimal horasContratadas = semanas
                .multiply(BigDecimal.valueOf(i.getHorasContratoSemanales()))
                .setScale(2, RoundingMode.HALF_UP);

        // TODO Sprint 11: invocar ms-asignaciones para obtener horas reales (clases COMPLETADAS)
        BigDecimal horasCumplidas = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        BigDecimal horasRestantes = horasContratadas.subtract(horasCumplidas).max(BigDecimal.ZERO);

        BigDecimal porcentaje = BigDecimal.ZERO;
        if (horasContratadas.signum() > 0) {
            porcentaje = horasCumplidas
                    .multiply(BigDecimal.valueOf(100))
                    .divide(horasContratadas, 2, RoundingMode.HALF_UP);
        }

        BigDecimal sueldoEstimado = estimarSueldo(i, horasCumplidas, semanas);

        String observacion = "Las horas cumplidas se integraran con ms-asignaciones (pendiente Sprint 11). " +
                "Mientras tanto se reporta 0; el sueldo estimado para TC/MT se basa en el salario mensual proporcional.";

        log.debug("Resumen horas instructor={} desde={} hasta={} contratadas={} cumplidas={}",
                instructorId, desde, hasta, horasContratadas, horasCumplidas);

        return new ResumenHorasResponse(
                instructorId,
                desde,
                hasta,
                i.getTipoContrato(),
                i.getHorasContratoSemanales(),
                horasContratadas,
                horasCumplidas,
                horasRestantes,
                porcentaje,
                sueldoEstimado,
                observacion
        );
    }

    private BigDecimal estimarSueldo(Instructor i, BigDecimal horasCumplidas, BigDecimal semanas) {
        if ("POR_HORAS".equals(i.getTipoContrato())) {
            if (i.getTarifaHora() == null) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            return horasCumplidas.multiply(i.getTarifaHora()).setScale(2, RoundingMode.HALF_UP);
        }
        // TC / MT: salario mensual proporcional a las semanas del rango (4.345 semanas/mes promedio)
        if (i.getSalarioMensual() == null) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal meses = semanas.divide(new BigDecimal("4.345"), 4, RoundingMode.HALF_UP);
        return i.getSalarioMensual().multiply(meses).setScale(2, RoundingMode.HALF_UP);
    }
}
