package com.escuela.asignaciones.service;

import com.escuela.asignaciones.dto.CreateAsignacionRequest;
import com.escuela.asignaciones.dto.UpdateAsignacionRequest;
import com.escuela.asignaciones.dto.UpdateAsignacionReprogramarRequest;
import com.escuela.asignaciones.dto.AsignacionListResponse;
import com.escuela.asignaciones.dto.AsignacionResponse;
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
        try {
            var estudiante = estudianteClient.obtenerEstudiante(request.estudianteId());
            // Solo se permite asignar clases a estudiantes que ya pagaron por
            // completo (MATRICULADO) o que ya tienen clases activas (CURSANDO).
            // PRE_MATRICULADO se rechaza porque NO ha completado el pago.
            // COMPLETADO/RETIRADO se rechazan porque el curso ya termino.
            String estado = estudiante.estado();
            if (!"MATRICULADO".equals(estado) && !"CURSANDO".equals(estado)) {
                throw new EstudianteInactivoException(request.estudianteId());
            }
        } catch (Exception e) {
            if (e instanceof EstudianteInactivoException) throw e;
            throw new EstudianteNoEncontradoException(request.estudianteId());
        }

        try {
            var instructor = instructorClient.obtenerInstructor(request.instructorId());
            if (!instructor.estado().equals("ACTIVO")) {
                throw new InstructorInactivoException(request.instructorId());
            }
        } catch (Exception e) {
            if (e instanceof InstructorInactivoException) throw e;
            throw new InstructorNoEncontradoException(request.instructorId());
        }

        try {
            var vehiculo = vehiculoClient.obtenerVehiculo(request.vehiculoId());
            if (vehiculo.deletedAt() != null) {
                throw new VehiculoEliminadoException(request.vehiculoId());
            }
        } catch (Exception e) {
            if (e instanceof VehiculoEliminadoException) throw e;
            throw new VehiculoNoEncontradoException(request.vehiculoId());
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
}
