package com.escuela.asignaciones.service;

import com.escuela.asignaciones.dto.CreateAsignacionRequest;
import com.escuela.asignaciones.dto.FinalizarAsignacionRequest;
import com.escuela.asignaciones.dto.IniciarAsignacionRequest;
import com.escuela.asignaciones.dto.RecorridoResponse;
import com.escuela.asignaciones.dto.UpdateAsignacionRequest;
import com.escuela.asignaciones.dto.UpdateAsignacionReprogramarRequest;
import com.escuela.asignaciones.dto.AsignacionListResponse;
import com.escuela.asignaciones.dto.AsignacionResponse;
import com.escuela.asignaciones.dto.feign.ActualizarKilometrajeFeignRequest;
import com.escuela.asignaciones.dto.feign.ActualizarKilometrajeFeignResponse;
import com.escuela.asignaciones.dto.feign.DisponibilidadDelDiaDTO;
import com.escuela.asignaciones.dto.feign.EstudianteDetailDTO;
import com.escuela.asignaciones.dto.feign.IncrementarHorasFeignRequest;
import com.escuela.asignaciones.dto.feign.IncrementarHorasFeignResponse;
import com.escuela.asignaciones.dto.feign.VehiculoDetailDTO;
import com.escuela.asignaciones.entity.Asignacion;
import com.escuela.asignaciones.entity.HistorialEstado;
import com.escuela.asignaciones.exception.*;
import com.escuela.asignaciones.feign.EstudianteClient;
import com.escuela.asignaciones.feign.InstructorClient;
import com.escuela.asignaciones.feign.VehiculoClient;
import com.escuela.asignaciones.mapper.AsignacionMapper;
import com.escuela.asignaciones.mapper.AsignacionMapperImpl;
import com.escuela.asignaciones.repository.AsignacionRepository;
import com.escuela.asignaciones.repository.HistorialEstadoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@Transactional
public class AsignacionServiceImpl implements AsignacionService {

    private final AsignacionRepository repository;
    private final HistorialEstadoRepository historialRepository;
    private AsignacionMapper mapper;
    private final EstudianteClient estudianteClient;
    private final InstructorClient instructorClient;
    private final VehiculoClient vehiculoClient;
    private final AsignacionEventDispatcher eventDispatcher;

    public AsignacionServiceImpl(AsignacionRepository repository, HistorialEstadoRepository historialRepository,
                                EstudianteClient estudianteClient,
                                InstructorClient instructorClient, VehiculoClient vehiculoClient,
                                AsignacionEventDispatcher eventDispatcher) {
        this.repository = repository;
        this.historialRepository = historialRepository;
        this.estudianteClient = estudianteClient;
        this.instructorClient = instructorClient;
        this.vehiculoClient = vehiculoClient;
        this.eventDispatcher = eventDispatcher;
    }

    private AsignacionMapper getMapper() {
        if (mapper == null) {
            this.mapper = new AsignacionMapperImpl();
        }
        return mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AsignacionListResponse> findAll(Pageable pageable) {
        return repository.findByDeletedAtIsNull(pageable).map(getMapper()::toListResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AsignacionListResponse> findByEstudianteId(Long estudianteId, Pageable pageable) {
        return repository.findByEstudianteIdAndDeletedAtIsNull(estudianteId, pageable)
                .map(getMapper()::toListResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AsignacionResponse findById(Long id) {
        Asignacion asignacion = repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AsignacionNotFoundException(id));
        return getMapper().toResponse(asignacion);
    }

    @Override
    public AsignacionResponse create(CreateAsignacionRequest request) {
        validarEntidadesExisten(request);
        validarConflictosTemporales(request);

        Asignacion asignacion = getMapper().toEntity(request);
        asignacion.setEstado("CONFIRMADA");
        if (asignacion.getTipoClase() == null) {
            asignacion.setTipoClase("PRACTICA");
        }
        asignacion = repository.save(asignacion);

        eventDispatcher.publishCreada(asignacion);

        log.info("Asignación creada id={}", asignacion.getId());
        return getMapper().toResponse(asignacion);
    }

    @Override
    public AsignacionResponse update(Long id, UpdateAsignacionRequest request) {
        Asignacion asignacion = repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AsignacionNotFoundException(id));

        getMapper().updateEntity(request, asignacion);
        asignacion.setUpdatedAt(LocalDateTime.now());
        asignacion = repository.save(asignacion);
        log.info("Asignación actualizada id={}", id);
        return getMapper().toResponse(asignacion);
    }

    @Override
    public void softDelete(Long id) {
        Asignacion asignacion = repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AsignacionNotFoundException(id));
        asignacion.setDeletedAt(LocalDateTime.now());
        repository.save(asignacion);
        log.info("Asignación soft-deleted id={}", id);
    }

    @Override
    public AsignacionResponse reprogramar(Long id, UpdateAsignacionReprogramarRequest request) {
        Asignacion asignacion = repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AsignacionNotFoundException(id));

        LocalDateTime fechaHoraAnterior = asignacion.getFechaHora();

        LocalDateTime nuevaFechaHora = request.fecha().atTime(request.horaInicio());
        Short nuevaDuracion = (short) ChronoUnit.MINUTES.between(request.horaInicio(), request.horaFin());

        validarConflictosTemporalesParaReprogramacion(id, asignacion, request.fecha(), request.horaInicio(), request.horaFin());

        asignacion.setFechaHora(nuevaFechaHora);
        asignacion.setDuracionMinutos(nuevaDuracion);
        asignacion.setUpdatedAt(LocalDateTime.now());

        asignacion = repository.save(asignacion);

        registrarHistorial(asignacion, "CONFIRMADA", "CONFIRMADA", "Reprogramación de clase");

        eventDispatcher.publishReprogramada(asignacion, fechaHoraAnterior);

        log.info("Asignación reprogramada id={}", id);
        return getMapper().toResponse(asignacion);
    }

    private void validarEntidadesExisten(CreateAsignacionRequest request) {
        // --- ESTUDIANTE ---
        EstudianteDetailDTO estudiante;
        try {
            estudiante = estudianteClient.obtenerEstudiante(request.estudianteId());
        } catch (Exception e) {
            throw new EstudianteNoEncontradoException(request.estudianteId());
        }
        // Solo MATRICULADO o CURSANDO pueden recibir clases
        String estadoEst = estudiante.estado();
        if (!"MATRICULADO".equals(estadoEst) && !"CURSANDO".equals(estadoEst)) {
            throw new EstudianteInactivoException(request.estudianteId());
        }
        if (estudiante.categoriaLicenciaId() == null) {
            throw new DisponibilidadException(
                    "El estudiante #" + request.estudianteId() +
                    " no tiene categoría de licencia asignada. Asigna primero la categoría en su perfil.");
        }

        // --- INSTRUCTOR ---
        var instructor = instructorClient.obtenerInstructor(request.instructorId());
        if (instructor == null || instructor.estado() == null) {
            throw new InstructorNoEncontradoException(request.instructorId());
        }
        if (!"ACTIVO".equals(instructor.estado())) {
            throw new InstructorInactivoException(request.instructorId());
        }

        // --- VEHÍCULO ---
        VehiculoDetailDTO vehiculo;
        try {
            vehiculo = vehiculoClient.obtenerVehiculo(request.vehiculoId());
        } catch (Exception e) {
            throw new VehiculoNoEncontradoException(request.vehiculoId());
        }
        if (vehiculo.deletedAt() != null) {
            throw new VehiculoEliminadoException(request.vehiculoId());
        }
        // Validaciones nuevas: estado operativo, SOAT, RTV, categoría
        validarVehiculoOperativo(vehiculo, request);
        validarCategoriaLicencia(estudiante, vehiculo);

        // --- DISPONIBILIDAD REAL DEL INSTRUCTOR (horario semanal + excepciones) ---
        validarDisponibilidadInstructor(request);
    }

    /**
     * Rechaza si el vehículo no está operativo o tiene documentos vencidos.
     */
    private void validarVehiculoOperativo(VehiculoDetailDTO v, CreateAsignacionRequest request) {
        if (v.estado() != null && !"ACTIVO".equals(v.estado())) {
            throw new DisponibilidadException(
                    "El vehículo " + (v.placa() != null ? v.placa() : "#" + v.id()) +
                    " no está disponible (estado: " + v.estado() + ")");
        }
        java.time.LocalDate hoy = java.time.LocalDate.now();
        if (v.soatVencimiento() != null && v.soatVencimiento().isBefore(hoy)) {
            throw new DisponibilidadException(
                    "El vehículo " + (v.placa() != null ? v.placa() : "#" + v.id()) +
                    " tiene el SOAT vencido (caducó el " + v.soatVencimiento() + "). " +
                    "Renueva el SOAT antes de programar clases.");
        }
        if (v.revisionVencimiento() != null && v.revisionVencimiento().isBefore(hoy)) {
            throw new DisponibilidadException(
                    "El vehículo " + (v.placa() != null ? v.placa() : "#" + v.id()) +
                    " tiene la Revisión Técnica vencida (caducó el " + v.revisionVencimiento() + "). " +
                    "Renueva la RTV antes de programar clases.");
        }
        if (v.categoriaLicenciaId() == null) {
            throw new DisponibilidadException(
                    "El vehículo " + (v.placa() != null ? v.placa() : "#" + v.id()) +
                    " no tiene categoría de licencia asignada. Edita su ficha primero.");
        }
    }

    /**
     * Rechaza si la categoría del vehículo no coincide con la que está aprendiendo el estudiante.
     * Ejemplo: estudiante de categoría B no puede aprender en un camión de categoría C.
     */
    private void validarCategoriaLicencia(EstudianteDetailDTO e, VehiculoDetailDTO v) {
        if (!e.categoriaLicenciaId().equals(v.categoriaLicenciaId())) {
            throw new DisponibilidadException(
                    "La categoría del vehículo (id=" + v.categoriaLicenciaId() +
                    ") no coincide con la del estudiante (id=" + e.categoriaLicenciaId() +
                    "). Selecciona un vehículo de la misma categoría.");
        }
    }

    /**
     * Rechaza si el instructor no está disponible en la fecha/hora pedida:
     *  - No tiene franjas configuradas ese día de la semana (horario recurrente)
     *  - Tiene una AUSENCIA puntual ese día
     *  - El horario solicitado no cae dentro de ninguna franja
     */
    private void validarDisponibilidadInstructor(CreateAsignacionRequest request) {
        DisponibilidadDelDiaDTO disp;
        try {
            disp = instructorClient.obtenerDisponibilidad(request.instructorId(), request.fecha());
        } catch (Exception ex) {
            throw new DisponibilidadException(
                    "No se pudo consultar la disponibilidad del instructor: " + ex.getMessage());
        }
        if (disp == null || Boolean.FALSE.equals(disp.disponible())) {
            String motivo = disp != null && disp.motivoNoDisponible() != null
                    ? disp.motivoNoDisponible()
                    : "instructor sin horario configurado o con AUSENCIA ese día";
            throw new DisponibilidadException(
                    "El instructor no está disponible el " + request.fecha() + ": " + motivo);
        }
        // Verificar que la hora solicitada cae dentro de alguna franja del día
        if (disp.franjas() == null || disp.franjas().isEmpty()) {
            throw new DisponibilidadException(
                    "El instructor no tiene franjas configuradas el " + request.fecha() +
                    ". Configura su horario semanal primero.");
        }
        boolean dentroDeFranja = disp.franjas().stream().anyMatch(f ->
                !request.horaInicio().isBefore(f.horaInicio()) &&
                !request.horaFin().isAfter(f.horaFin()));
        if (!dentroDeFranja) {
            String franjasStr = disp.franjas().stream()
                    .map(f -> f.horaInicio() + "-" + f.horaFin())
                    .reduce((a, b) -> a + ", " + b).orElse("");
            throw new DisponibilidadException(
                    "El horario solicitado (" + request.horaInicio() + "-" + request.horaFin() +
                    ") no está dentro del horario del instructor ese día. " +
                    "Franjas disponibles: " + franjasStr);
        }
    }

    private void validarConflictosTemporales(CreateAsignacionRequest request) {
        LocalDateTime fechaHora = request.fecha().atTime(request.horaInicio());
        LocalDateTime horaFin = request.fecha().atTime(request.horaFin());

        long conflictosInstructor = repository.countByInstructorIdAndFechaHoraBetweenAndEstadoAndDeletedAtIsNull(
                request.instructorId(), fechaHora, horaFin, "CONFIRMADA"
        );
        if (conflictosInstructor > 0) {
            throw new DisponibilidadException("Instructor no disponible en esa fecha y hora");
        }

        long conflictosVehiculo = repository.countByVehiculoIdAndFechaHoraBetweenAndEstadoAndDeletedAtIsNull(
                request.vehiculoId(), fechaHora, horaFin, "CONFIRMADA"
        );
        if (conflictosVehiculo > 0) {
            throw new DisponibilidadException("Vehículo no disponible en esa fecha y hora");
        }
    }

    private void validarConflictosTemporalesParaReprogramacion(Long asignacionId, Asignacion asignacion,
                                                                java.time.LocalDate fecha,
                                                                LocalTime horaInicio, LocalTime horaFin) {
        LocalDateTime nuevaFechaHora = fecha.atTime(horaInicio);
        LocalDateTime nuevaHoraFin = fecha.atTime(horaFin);

        long conflictosInstructor = repository.countByInstructorIdAndFechaHoraBetweenAndEstadoAndDeletedAtIsNullAndIdNot(
                asignacion.getInstructorId(), nuevaFechaHora, nuevaHoraFin, "CONFIRMADA", asignacionId
        );
        if (conflictosInstructor > 0) {
            throw new DisponibilidadException("Instructor no disponible en esa fecha y hora");
        }

        long conflictosVehiculo = repository.countByVehiculoIdAndFechaHoraBetweenAndEstadoAndDeletedAtIsNullAndIdNot(
                asignacion.getVehiculoId(), nuevaFechaHora, nuevaHoraFin, "CONFIRMADA", asignacionId
        );
        if (conflictosVehiculo > 0) {
            throw new DisponibilidadException("Vehículo no disponible en esa fecha y hora");
        }
    }

    private void registrarHistorial(Asignacion asignacion, String estadoAnterior, String estadoNuevo, String observaciones) {
        HistorialEstado historial = HistorialEstado.builder()
                .asignacion(asignacion)
                .estadoAnterior(estadoAnterior)
                .estadoNuevo(estadoNuevo)
                .fechaCambio(LocalDateTime.now())
                .observaciones(observaciones)
                .build();
        historialRepository.save(historial);
    }

    // =========================================================================
    //  KILOMETRAJE: iniciar / finalizar / recorrido
    // =========================================================================

    @Override
    public RecorridoResponse iniciar(Long id, IniciarAsignacionRequest request) {
        Asignacion a = repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AsignacionNotFoundException(id));

        if ("COMPLETADA".equals(a.getEstado()) || "CANCELADA".equals(a.getEstado())) {
            throw new IllegalStateException("No se puede iniciar una clase en estado " + a.getEstado());
        }
        if (a.getHoraInicioReal() != null) {
            throw new IllegalStateException("La clase ya fue iniciada el " + a.getHoraInicioReal());
        }

        // Si no se envia km_inicial, usar el km actual del vehiculo como referencia
        Integer kmInicial = request.kmInicial();
        if (kmInicial == null) {
            VehiculoDetailDTO v = vehiculoClient.obtenerVehiculo(a.getVehiculoId());
            kmInicial = (v != null && v.kilometraje() != null) ? v.kilometraje() : 0;
            log.info("kmInicial no enviado, tomado del vehiculo: vehiculoId={} km={}",
                    a.getVehiculoId(), kmInicial);
        }

        String estadoAnterior = a.getEstado();
        a.setEstado("EN_CURSO");
        a.setKmInicial(kmInicial);
        a.setHoraInicioReal(LocalDateTime.now());
        if (request.observaciones() != null && !request.observaciones().isBlank()) {
            a.setObservacionesRecorrido(request.observaciones());
        }
        a.setUpdatedAt(LocalDateTime.now());
        a = repository.save(a);

        registrarHistorial(a, estadoAnterior, "EN_CURSO", "Clase iniciada, km=" + kmInicial);
        log.info("Asignacion iniciada id={} kmInicial={}", id, kmInicial);

        return toRecorridoResponse(a, null, null, null, null);
    }

    @Override
    public RecorridoResponse finalizar(Long id, FinalizarAsignacionRequest request) {
        Asignacion a = repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AsignacionNotFoundException(id));

        if ("COMPLETADA".equals(a.getEstado())) {
            throw new IllegalStateException("La clase ya está COMPLETADA");
        }
        if ("CANCELADA".equals(a.getEstado())) {
            throw new IllegalStateException("No se puede finalizar una clase CANCELADA");
        }

        // Si el instructor olvido marcar inicio, lo hacemos ahora (km_inicial = km actual)
        if (a.getKmInicial() == null) {
            VehiculoDetailDTO v = vehiculoClient.obtenerVehiculo(a.getVehiculoId());
            Integer kmActual = (v != null && v.kilometraje() != null) ? v.kilometraje() : 0;
            a.setKmInicial(kmActual);
            a.setHoraInicioReal(LocalDateTime.now()); // best-effort: marca el "inicio" en el mismo momento
            log.warn("Finalizando sin inicio previo. Asignacion id={} kmInicial asumido={}", id, kmActual);
        }

        if (request.kmFinal() < a.getKmInicial()) {
            throw new IllegalArgumentException(
                    "kmFinal (" + request.kmFinal() + ") debe ser >= kmInicial (" + a.getKmInicial() + ")");
        }

        String estadoAnterior = a.getEstado();
        a.setEstado("COMPLETADA");
        a.setKmFinal(request.kmFinal());
        a.setHoraFinReal(LocalDateTime.now());
        if (request.observacionesRecorrido() != null && !request.observacionesRecorrido().isBlank()) {
            String previo = a.getObservacionesRecorrido() == null ? "" : a.getObservacionesRecorrido() + "\n";
            a.setObservacionesRecorrido(previo + request.observacionesRecorrido());
        }
        a.setUpdatedAt(LocalDateTime.now());
        a = repository.save(a);

        registrarHistorial(a, estadoAnterior, "COMPLETADA",
                "Clase finalizada. km " + a.getKmInicial() + " -> " + a.getKmFinal()
                        + " (" + (a.getKmFinal() - a.getKmInicial()) + " km recorridos)");

        // Sync odometro del vehiculo (best-effort; circuit breaker maneja fallos)
        boolean syncVehOk;
        String syncVehMsg;
        try {
            ActualizarKilometrajeFeignResponse resp = vehiculoClient.actualizarKilometraje(
                    a.getVehiculoId(),
                    new ActualizarKilometrajeFeignRequest(a.getKmFinal(), "ASIGNACION_" + a.getId()));
            syncVehOk = Boolean.TRUE.equals(resp.aplicado());
            syncVehMsg = resp.observacion();
            log.info("Sync km vehiculoId={} resultado={} msg={}", a.getVehiculoId(), syncVehOk, syncVehMsg);
        } catch (Exception ex) {
            syncVehOk = false;
            syncVehMsg = "Sync fallo: " + ex.getMessage();
            log.warn("Sync km vehiculoId={} excepcion={}", a.getVehiculoId(), ex.getMessage());
        }

        // Sync horas completadas del estudiante (sumar minutos reales de la clase)
        boolean syncEstOk = false;
        String syncEstMsg = null;
        long duracionMin = (a.getHoraInicioReal() != null && a.getHoraFinReal() != null)
                ? ChronoUnit.MINUTES.between(a.getHoraInicioReal(), a.getHoraFinReal())
                : (a.getDuracionMinutos() != null ? a.getDuracionMinutos() : 0);
        // Si la duracion real es 0 (clase relámpago), usar la duración programada como mínimo
        if (duracionMin <= 0 && a.getDuracionMinutos() != null) {
            duracionMin = a.getDuracionMinutos();
        }
        if (duracionMin > 0) {
            try {
                IncrementarHorasFeignResponse respH = estudianteClient.incrementarHoras(
                        a.getEstudianteId(),
                        new IncrementarHorasFeignRequest((int) duracionMin, "ASIGNACION_" + a.getId()));
                syncEstOk = respH.minutosActuales() != null;
                syncEstMsg = respH.observacion();
                log.info("Sync horas estudianteId={} {} -> {} transicion={}",
                        a.getEstudianteId(), respH.minutosAnteriores(), respH.minutosActuales(), respH.transicionAutomatica());
            } catch (Exception ex) {
                syncEstMsg = "Sync horas fallo: " + ex.getMessage();
                log.warn("Sync horas estudianteId={} excepcion={}", a.getEstudianteId(), ex.getMessage());
            }
        } else {
            syncEstMsg = "Duración 0, no se sumaron minutos";
        }

        // Publica AsignacionCompletada para que ms-estudiantes mantenga el contador
        // clases_completadas en progreso_academico (camino asincrono, separado del
        // sync de horas via Feign que es transaccional con la operacion).
        Integer kmRec = (a.getKmInicial() != null && a.getKmFinal() != null)
                ? (a.getKmFinal() - a.getKmInicial()) : null;
        eventDispatcher.publishCompletada(a, (int) duracionMin, kmRec);

        return toRecorridoResponse(a, syncVehOk, syncVehMsg, syncEstOk, syncEstMsg);
    }

    @Override
    @Transactional(readOnly = true)
    public RecorridoResponse obtenerRecorrido(Long id) {
        Asignacion a = repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AsignacionNotFoundException(id));
        return toRecorridoResponse(a, null, null, null, null);
    }

    private RecorridoResponse toRecorridoResponse(Asignacion a,
                                                  Boolean syncVehOk, String syncVehMsg,
                                                  Boolean syncEstOk, String syncEstMsg) {
        Integer kmRecorridos = (a.getKmInicial() != null && a.getKmFinal() != null)
                ? (a.getKmFinal() - a.getKmInicial()) : null;
        Long duracionReal = (a.getHoraInicioReal() != null && a.getHoraFinReal() != null)
                ? ChronoUnit.MINUTES.between(a.getHoraInicioReal(), a.getHoraFinReal()) : null;
        return new RecorridoResponse(
                a.getId(),
                a.getVehiculoId(),
                a.getEstado(),
                a.getKmInicial(),
                a.getKmFinal(),
                kmRecorridos,
                a.getHoraInicioReal(),
                a.getHoraFinReal(),
                duracionReal,
                a.getObservacionesRecorrido(),
                syncVehOk,
                syncVehMsg,
                syncEstOk,
                syncEstMsg
        );
    }
}
