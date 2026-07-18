package com.escuela.vehiculos.service;

import com.escuela.common.events.vehiculos.SoatVencimientoProximoEvent;
import com.escuela.vehiculos.dto.AlertaSoatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Scheduler que revisa diariamente los vehiculos con SOAT vencido o proximo a vencer
 * y publica {@link SoatVencimientoProximoEvent} por cada uno.
 *
 * <p>Corre a las 08:00 hora local del servidor.</p>
 *
 * <p>Idempotencia: el {@code eventId} se genera de forma deterministica a partir de
 * (vehiculoId, fecha del dia). Si el scheduler se ejecuta mas de una vez el mismo dia,
 * el consumidor descartara los duplicados via {@code IdempotencyStore}.</p>
 */
@Service
public class SoatAlertaScheduler {

    private static final Logger log = LoggerFactory.getLogger(SoatAlertaScheduler.class);
    private static final int DIAS_LOOKAHEAD = 30;

    private final AlertaSoatService alertaSoatService;
    private final VehiculoEventDispatcher dispatcher;

    public SoatAlertaScheduler(AlertaSoatService alertaSoatService, VehiculoEventDispatcher dispatcher) {
        this.alertaSoatService = alertaSoatService;
        this.dispatcher = dispatcher;
    }

    @Scheduled(cron = "${vehiculos.scheduler.soat.cron:0 0 8 * * *}")
    public void publicarAlertasSoat() {
        LocalDate hoy = LocalDate.now();
        List<AlertaSoatResponse> alertas = alertaSoatService.alertasSoat(DIAS_LOOKAHEAD);
        log.info("[SOAT SCHEDULER] {} vehiculos con SOAT vencido/proximo a vencer", alertas.size());

        for (AlertaSoatResponse a : alertas) {
            UUID eventId = deterministicEventId(a.vehiculoId(), hoy);
            SoatVencimientoProximoEvent event = SoatVencimientoProximoEvent.builder()
                    .eventId(eventId)
                    .timestamp(Instant.now())
                    .vehiculoId(a.vehiculoId())
                    .placa(a.placa())
                    .marca(a.marca())
                    .modelo(a.modelo())
                    .soatVencimiento(a.soatVencimiento())
                    .diasParaVencer((int) a.diasParaVencer())
                    .vencido(Boolean.TRUE.equals(a.vencido()))
                    .build();
            dispatcher.publishSoatVencimientoProximo(event);
        }
        log.info("[SOAT SCHEDULER] Publicados {} eventos de vencimiento SOAT", alertas.size());
    }

    private static UUID deterministicEventId(Long vehiculoId, LocalDate fecha) {
        String seed = "SOAT-" + vehiculoId + "-" + fecha;
        return UUID.nameUUIDFromBytes(seed.getBytes(StandardCharsets.UTF_8));
    }
}
