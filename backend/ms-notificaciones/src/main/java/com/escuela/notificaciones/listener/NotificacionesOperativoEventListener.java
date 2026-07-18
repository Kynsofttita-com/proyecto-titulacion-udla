package com.escuela.notificaciones.listener;

import com.escuela.common.events.cobros.PagoAtrasadoEvent;
import com.escuela.common.events.estudiantes.CursoCompletadoEvent;
import com.escuela.common.events.idempotency.IdempotencyStore;
import com.escuela.common.events.instructores.LicenciaVencimientoProximoEvent;
import com.escuela.common.events.listener.AbstractEventListener;
import com.escuela.common.events.vehiculos.SoatVencimientoProximoEvent;
import com.escuela.notificaciones.config.RabbitConfig;
import com.escuela.notificaciones.service.NotificacionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

/**
 * Listener multiplexer de eventos operativos (SOAT, licencia, curso completado, pago
 * atrasado) sobre la queue de notificaciones.
 *
 * <p>Persiste una fila por cada admin/staff en {@code notificaciones}; para los eventos
 * con usuario asociado (licencia del instructor, curso del estudiante, pago del
 * estudiante) tambien crea una fila para el propio usuario.</p>
 *
 * <p>NOTA: reside en la misma queue que {@link NotificacionesAuthEventListener} pero
 * son clases distintas registradas en {@link RabbitListener}. Spring AMQP los combina
 * en una unica cadena de {@code @RabbitHandler} porque comparten queue.</p>
 */
@Component
@Profile("!test")
@RabbitListener(queues = RabbitConfig.OPERATIVO_QUEUE_NAME)
public class NotificacionesOperativoEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificacionesOperativoEventListener.class);
    private static final String MICROSERVICIO = "ms-notificaciones-operativo";
    private static final DateTimeFormatter FECHA_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final NotificacionService notificacionService;

    private final SoatVencimientoHandler soatHandler;
    private final LicenciaVencimientoHandler licenciaHandler;
    private final CursoCompletadoHandler cursoHandler;
    private final PagoAtrasadoHandler pagoAtrasadoHandler;

    public NotificacionesOperativoEventListener(IdempotencyStore idempotencyStore,
                                                NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
        this.soatHandler = new SoatVencimientoHandler(idempotencyStore);
        this.licenciaHandler = new LicenciaVencimientoHandler(idempotencyStore);
        this.cursoHandler = new CursoCompletadoHandler(idempotencyStore);
        this.pagoAtrasadoHandler = new PagoAtrasadoHandler(idempotencyStore);
    }

    @RabbitHandler
    public void onSoatVencimiento(SoatVencimientoProximoEvent event) {
        soatHandler.process(event);
    }

    @RabbitHandler
    public void onLicenciaVencimiento(LicenciaVencimientoProximoEvent event) {
        licenciaHandler.process(event);
    }

    @RabbitHandler
    public void onCursoCompletado(CursoCompletadoEvent event) {
        cursoHandler.process(event);
    }

    @RabbitHandler
    public void onPagoAtrasado(PagoAtrasadoEvent event) {
        pagoAtrasadoHandler.process(event);
    }

    // -----------------------------------------------------------------------
    // Handlers internos
    // -----------------------------------------------------------------------

    private class SoatVencimientoHandler extends AbstractEventListener<SoatVencimientoProximoEvent> {
        SoatVencimientoHandler(IdempotencyStore store) { super(store, MICROSERVICIO); }

        public void process(SoatVencimientoProximoEvent event) {
            processWithIdempotency(event, SoatVencimientoProximoEvent.ROUTING_KEY);
        }

        @Override
        protected void handle(SoatVencimientoProximoEvent event) {
            String titulo = event.isVencido()
                    ? String.format("SOAT VENCIDO: %s", event.getPlaca())
                    : String.format("SOAT proximo a vencer: %s", event.getPlaca());

            String mensaje = event.isVencido()
                    ? String.format("El SOAT del vehiculo %s (%s %s) esta vencido desde el %s (%d dias).",
                            event.getPlaca(), event.getMarca(), event.getModelo(),
                            FECHA_FORMATTER.format(event.getSoatVencimiento()),
                            Math.abs(event.getDiasParaVencer()))
                    : String.format("El SOAT del vehiculo %s (%s %s) vence el %s. Faltan %d dias.",
                            event.getPlaca(), event.getMarca(), event.getModelo(),
                            FECHA_FORMATTER.format(event.getSoatVencimiento()),
                            event.getDiasParaVencer());

            String prioridad = event.isVencido() ? "ALTA" :
                    (event.getDiasParaVencer() <= 7 ? "ALTA" : "NORMAL");

            int total = notificacionService.crearNotificacionParaAdmins(
                    titulo, mensaje, "SOAT_VENCIMIENTO", prioridad);
            log.info("SOAT vencimiento notificado a {} admin/staff (vehiculoId={}, placa={})",
                    total, event.getVehiculoId(), event.getPlaca());
        }
    }

    private class LicenciaVencimientoHandler extends AbstractEventListener<LicenciaVencimientoProximoEvent> {
        LicenciaVencimientoHandler(IdempotencyStore store) { super(store, MICROSERVICIO); }

        public void process(LicenciaVencimientoProximoEvent event) {
            processWithIdempotency(event, LicenciaVencimientoProximoEvent.ROUTING_KEY);
        }

        @Override
        protected void handle(LicenciaVencimientoProximoEvent event) {
            String titulo = event.isVencida()
                    ? String.format("Licencia VENCIDA: %s", event.getNombreCompleto())
                    : String.format("Licencia proxima a vencer: %s", event.getNombreCompleto());

            String mensaje = event.isVencida()
                    ? String.format("La licencia (%s) de %s esta vencida desde el %s (%d dias).",
                            event.getLicenciaCategoria(), event.getNombreCompleto(),
                            FECHA_FORMATTER.format(event.getLicenciaCaducidad()),
                            Math.abs(event.getDiasParaVencer()))
                    : String.format("La licencia (%s) de %s vence el %s. Faltan %d dias.",
                            event.getLicenciaCategoria(), event.getNombreCompleto(),
                            FECHA_FORMATTER.format(event.getLicenciaCaducidad()),
                            event.getDiasParaVencer());

            String prioridad = event.isVencida() ? "ALTA" :
                    (event.getDiasParaVencer() <= 7 ? "ALTA" : "NORMAL");

            int total = notificacionService.crearNotificacionParaAdmins(
                    titulo, mensaje, "LICENCIA_VENCIMIENTO", prioridad);
            if (event.getUsuarioIdInstructor() != null) {
                notificacionService.crearNotificacion(
                        event.getUsuarioIdInstructor(),
                        event.isVencida() ? "Tu licencia esta vencida" : "Tu licencia esta por vencer",
                        mensaje,
                        "LICENCIA_VENCIMIENTO",
                        prioridad);
                total++;
            }
            log.info("Licencia vencimiento notificado a {} usuarios (instructorId={})",
                    total, event.getInstructorId());
        }
    }

    private class CursoCompletadoHandler extends AbstractEventListener<CursoCompletadoEvent> {
        CursoCompletadoHandler(IdempotencyStore store) { super(store, MICROSERVICIO); }

        public void process(CursoCompletadoEvent event) {
            processWithIdempotency(event, CursoCompletadoEvent.ROUTING_KEY);
        }

        @Override
        protected void handle(CursoCompletadoEvent event) {
            String tituloAdmin = String.format("Curso completado: %s", event.getNombreCompleto());
            String mensajeAdmin = String.format(
                    "El estudiante %s completo el curso %s (%dh de %dh) el %s.",
                    event.getNombreCompleto(),
                    event.getTipoCursoNombre() != null ? event.getTipoCursoNombre() : "asignado",
                    event.getHorasCompletadas(), event.getHorasRequeridas(),
                    FECHA_FORMATTER.format(event.getFechaCompletado()));

            int total = notificacionService.crearNotificacionParaAdmins(
                    tituloAdmin, mensajeAdmin, "CURSO_COMPLETADO", "NORMAL");

            if (event.getUsuarioIdEstudiante() != null) {
                String tituloEst = "Felicitaciones! Completaste tu curso";
                String mensajeEst = String.format(
                        "Has completado el 100%% de las horas requeridas (%dh). Ahora podes tramitar el examen ANT.",
                        event.getHorasRequeridas());
                notificacionService.crearNotificacion(
                        event.getUsuarioIdEstudiante(), tituloEst, mensajeEst,
                        "CURSO_COMPLETADO", "ALTA");
                total++;
            }
            log.info("Curso completado notificado a {} usuarios (estudianteId={})",
                    total, event.getEstudianteId());
        }
    }

    private class PagoAtrasadoHandler extends AbstractEventListener<PagoAtrasadoEvent> {
        PagoAtrasadoHandler(IdempotencyStore store) { super(store, MICROSERVICIO); }

        public void process(PagoAtrasadoEvent event) {
            processWithIdempotency(event, PagoAtrasadoEvent.ROUTING_KEY);
        }

        @Override
        protected void handle(PagoAtrasadoEvent event) {
            String tituloAdmin = String.format("Pago atrasado: %s", event.getNombreEstudiante());
            String mensajeAdmin = String.format(
                    "La factura %s de %s esta atrasada %d dias. Saldo pendiente: $%s de $%s.",
                    event.getNumeroFactura(), event.getNombreEstudiante(),
                    event.getDiasAtraso(),
                    event.getMontoPendiente(), event.getMontoTotal());

            String prioridad = event.getDiasAtraso() > 30 ? "ALTA" :
                    (event.getDiasAtraso() > 15 ? "ALTA" : "NORMAL");

            int total = notificacionService.crearNotificacionParaAdmins(
                    tituloAdmin, mensajeAdmin, "PAGO_ATRASADO", prioridad);

            if (event.getUsuarioIdEstudiante() != null) {
                String tituloEst = "Tu pago esta atrasado";
                String mensajeEst = String.format(
                        "La factura %s tiene %d dias de atraso. Saldo pendiente: $%s. " +
                        "Acercate a la escuela para regularizar tu situacion.",
                        event.getNumeroFactura(), event.getDiasAtraso(), event.getMontoPendiente());
                notificacionService.crearNotificacion(
                        event.getUsuarioIdEstudiante(), tituloEst, mensajeEst,
                        "PAGO_ATRASADO", prioridad);
                total++;
            }
            log.info("Pago atrasado notificado a {} usuarios (facturaId={})",
                    total, event.getFacturaId());
        }
    }
}
