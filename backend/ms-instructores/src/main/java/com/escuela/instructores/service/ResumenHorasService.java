package com.escuela.instructores.service;

import com.escuela.instructores.client.AsignacionesClient;
import com.escuela.instructores.dto.HorasCumplidasResponse;
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
 * fechas. Las horas cumplidas reales se obtienen de ms-asignaciones (suma
 * de duracion de clases COMPLETADA en el rango).
 */
@Service
@Transactional(readOnly = true)
public class ResumenHorasService {

    private static final Logger log = LoggerFactory.getLogger(ResumenHorasService.class);

    private final InstructorService instructorService;
    private final AsignacionesClient asignacionesClient;

    public ResumenHorasService(InstructorService instructorService,
                               AsignacionesClient asignacionesClient) {
        this.instructorService = instructorService;
        this.asignacionesClient = asignacionesClient;
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

        // Horas reales cumplidas — vienen de ms-asignaciones (clases COMPLETADA).
        // Si el llamado falla, degradamos a 0 con observacion (no rompemos la UI).
        BigDecimal horasCumplidas;
        long clasesCompletadas = 0;
        String observacion;
        try {
            HorasCumplidasResponse h = asignacionesClient.horasCumplidasInstructor(instructorId, desde, hasta);
            horasCumplidas = h.horasCumplidas() != null
                    ? h.horasCumplidas().setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            clasesCompletadas = h.clasesCompletadas();
            observacion = String.format("Basado en %d clase(s) COMPLETADA registradas en ms-asignaciones.",
                    clasesCompletadas);
        } catch (Exception ex) {
            log.warn("No se pudieron consultar horas cumplidas del instructor {}: {}", instructorId, ex.getMessage());
            horasCumplidas = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            observacion = "No se pudo consultar horas reales (ms-asignaciones no disponible).";
        }

        BigDecimal horasRestantes = horasContratadas.subtract(horasCumplidas).max(BigDecimal.ZERO);

        BigDecimal porcentaje = BigDecimal.ZERO;
        if (horasContratadas.signum() > 0) {
            porcentaje = horasCumplidas
                    .multiply(BigDecimal.valueOf(100))
                    .divide(horasContratadas, 2, RoundingMode.HALF_UP);
        }

        BigDecimal sueldoEstimado = estimarSueldo(i, horasCumplidas, semanas);

        log.debug("Resumen horas instructor={} desde={} hasta={} contratadas={} cumplidas={} clases={}",
                instructorId, desde, hasta, horasContratadas, horasCumplidas, clasesCompletadas);

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
