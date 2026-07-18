package com.escuela.instructores.service;

import com.escuela.common.events.instructores.LicenciaVencimientoProximoEvent;
import com.escuela.instructores.entity.Instructor;
import com.escuela.instructores.repository.InstructorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * Scheduler diario que publica {@link LicenciaVencimientoProximoEvent} por cada
 * instructor cuya licencia esta vencida o vencera en menos de 30 dias.
 *
 * <p>Corre a las 08:05 hora local (5 min despues del SOAT para escalonar carga).</p>
 */
@Service
public class LicenciaAlertaScheduler {

    private static final Logger log = LoggerFactory.getLogger(LicenciaAlertaScheduler.class);
    private static final int DIAS_LOOKAHEAD = 30;

    private final InstructorRepository instructorRepository;
    private final InstructorEventDispatcher dispatcher;

    public LicenciaAlertaScheduler(InstructorRepository instructorRepository,
                                   InstructorEventDispatcher dispatcher) {
        this.instructorRepository = instructorRepository;
        this.dispatcher = dispatcher;
    }

    @Scheduled(cron = "${instructores.scheduler.licencia.cron:0 5 8 * * *}")
    @Transactional(readOnly = true)
    public void publicarAlertasLicencia() {
        LocalDate hoy = LocalDate.now();
        LocalDate limite = hoy.plusDays(DIAS_LOOKAHEAD);
        List<Instructor> lista = instructorRepository.findConLicenciaPorVencerAntesDe(limite);
        log.info("[LICENCIA SCHEDULER] {} instructores con licencia vencida/proxima", lista.size());

        for (Instructor i : lista) {
            long dias = ChronoUnit.DAYS.between(hoy, i.getLicenciaCaducidad());
            boolean vencida = dias < 0;
            UUID eventId = deterministicEventId(i.getId(), hoy);
            LicenciaVencimientoProximoEvent event = LicenciaVencimientoProximoEvent.builder()
                    .eventId(eventId)
                    .timestamp(Instant.now())
                    .instructorId(i.getId())
                    .usuarioIdInstructor(i.getUsuarioId())
                    .cedula(i.getCedula())
                    .nombreCompleto(i.getNombre() + " " + i.getApellido())
                    .email(i.getEmail())
                    .licenciaCategoria(i.getLicenciaCategoria())
                    .licenciaCaducidad(i.getLicenciaCaducidad())
                    .diasParaVencer((int) dias)
                    .vencida(vencida)
                    .build();
            dispatcher.publishLicenciaVencimientoProximo(event);
        }
        log.info("[LICENCIA SCHEDULER] Publicados {} eventos de vencimiento licencia", lista.size());
    }

    private static UUID deterministicEventId(Long instructorId, LocalDate fecha) {
        String seed = "LICENCIA-" + instructorId + "-" + fecha;
        return UUID.nameUUIDFromBytes(seed.getBytes(StandardCharsets.UTF_8));
    }
}
