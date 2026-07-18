package com.escuela.cobros.service;

import com.escuela.cobros.client.EstudianteClient;
import com.escuela.cobros.dto.EstudianteDetailDTO;
import com.escuela.cobros.entity.Factura;
import com.escuela.cobros.repository.FacturaRepository;
import com.escuela.common.events.cobros.PagoAtrasadoEvent;
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
 * Scheduler diario que revisa facturas con cuota vencida y saldo > 0 y publica
 * {@link PagoAtrasadoEvent} por cada una.
 *
 * <p>Corre a las 08:10 hora local (5 min despues de licencias para escalonar carga).</p>
 */
@Service
public class PagoAtrasadoScheduler {

    private static final Logger log = LoggerFactory.getLogger(PagoAtrasadoScheduler.class);

    private final FacturaRepository facturaRepository;
    private final FacturaEventDispatcher dispatcher;
    private final EstudianteClient estudianteClient;

    public PagoAtrasadoScheduler(FacturaRepository facturaRepository,
                                 FacturaEventDispatcher dispatcher,
                                 EstudianteClient estudianteClient) {
        this.facturaRepository = facturaRepository;
        this.dispatcher = dispatcher;
        this.estudianteClient = estudianteClient;
    }

    @Scheduled(cron = "${cobros.scheduler.atrasos.cron:0 10 8 * * *}")
    @Transactional(readOnly = true)
    public void publicarPagosAtrasados() {
        LocalDate hoy = LocalDate.now();
        List<Factura> atrasadas = facturaRepository.findAtrasadas(hoy);
        log.info("[ATRASOS SCHEDULER] {} facturas con pago atrasado", atrasadas.size());

        for (Factura f : atrasadas) {
            EstudianteDetailDTO est;
            try {
                est = estudianteClient.obtenerEstudiante(f.getEstudianteId());
            } catch (Exception ex) {
                log.warn("Fallo consultar estudiante id={} para facturaId={}: {}",
                        f.getEstudianteId(), f.getId(), ex.getMessage());
                est = new EstudianteDetailDTO(f.getEstudianteId(), "DESCONOCIDO");
            }

            int diasAtraso = (int) ChronoUnit.DAYS.between(f.getFechaVencimiento(), hoy);
            UUID eventId = deterministicEventId(f.getId(), hoy);

            PagoAtrasadoEvent event = PagoAtrasadoEvent.builder()
                    .eventId(eventId)
                    .timestamp(Instant.now())
                    .facturaId(f.getId())
                    .numeroFactura(f.getNumeroFactura())
                    .estudianteId(f.getEstudianteId())
                    .usuarioIdEstudiante(est.usuarioId())
                    .cedulaEstudiante(est.cedula())
                    .nombreEstudiante(est.nombreCompleto())
                    .emailEstudiante(est.email())
                    .montoTotal(f.getMontoOriginal())
                    .montoPendiente(f.getSaldo())
                    .diasAtraso(diasAtraso)
                    .fechaVencimientoCuota(f.getFechaVencimiento())
                    .build();
            dispatcher.publishPagoAtrasado(event);
        }
        log.info("[ATRASOS SCHEDULER] Publicados {} eventos de pago atrasado", atrasadas.size());
    }

    private static UUID deterministicEventId(Long facturaId, LocalDate fecha) {
        String seed = "ATRASO-" + facturaId + "-" + fecha;
        return UUID.nameUUIDFromBytes(seed.getBytes(StandardCharsets.UTF_8));
    }
}
