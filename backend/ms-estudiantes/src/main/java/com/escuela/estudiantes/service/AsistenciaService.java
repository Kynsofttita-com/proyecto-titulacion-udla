package com.escuela.estudiantes.service;

import com.escuela.estudiantes.dto.AsistenciaResponse;
import com.escuela.estudiantes.dto.CreateAsistenciaRequest;
import com.escuela.estudiantes.dto.UpdateAsistenciaRequest;
import com.escuela.estudiantes.entity.Asistencia;
import com.escuela.estudiantes.entity.Estudiante;
import com.escuela.estudiantes.exception.EstudianteNotFoundException;
import com.escuela.estudiantes.exception.RecursoNotFoundException;
import com.escuela.estudiantes.repository.AsistenciaRepository;
import com.escuela.estudiantes.repository.EstudianteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AsistenciaService {

    private static final Logger log = LoggerFactory.getLogger(AsistenciaService.class);

    private final AsistenciaRepository asistenciaRepository;
    private final EstudianteRepository estudianteRepository;

    public AsistenciaService(AsistenciaRepository asistenciaRepository,
                             EstudianteRepository estudianteRepository) {
        this.asistenciaRepository = asistenciaRepository;
        this.estudianteRepository = estudianteRepository;
    }

    @Transactional(readOnly = true)
    public Page<AsistenciaResponse> listar(Long estudianteId, Pageable pageable) {
        verificarEstudianteExiste(estudianteId);
        return asistenciaRepository.findByEstudianteIdOrderByFechaClaseDesc(estudianteId, pageable)
                .map(this::toResponse);
    }

    public AsistenciaResponse registrar(Long estudianteId, CreateAsistenciaRequest request) {
        Estudiante estudiante = estudianteRepository.findByIdAndDeletedAtIsNull(estudianteId)
                .orElseThrow(() -> new EstudianteNotFoundException(estudianteId));

        // Una asistencia por estudiante x asignacion
        asistenciaRepository.findByEstudianteIdAndAsignacionId(estudianteId, request.asignacionId())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Ya existe asistencia para estudiante " + estudianteId +
                                    " en asignacion " + request.asignacionId());
                });

        Asistencia a = Asistencia.builder()
                .estudiante(estudiante)
                .asignacionId(request.asignacionId())
                .fechaClase(request.fechaClase())
                .asistio(request.asistio())
                .justificacion(request.justificacion())
                .observaciones(request.observaciones())
                .build();

        a = asistenciaRepository.save(a);
        log.info("Asistencia registrada id={} estudianteId={} asignacionId={} asistio={}",
                a.getId(), estudianteId, request.asignacionId(), request.asistio());
        return toResponse(a);
    }

    public AsistenciaResponse actualizar(Long estudianteId, Long asistenciaId,
                                         UpdateAsistenciaRequest request) {
        Asistencia a = asistenciaRepository.findByIdAndEstudianteId(asistenciaId, estudianteId)
                .orElseThrow(() -> new RecursoNotFoundException("Asistencia", asistenciaId));

        if (request.asistio() != null) a.setAsistio(request.asistio());
        if (request.justificacion() != null) a.setJustificacion(request.justificacion());
        if (request.observaciones() != null) a.setObservaciones(request.observaciones());

        asistenciaRepository.save(a);
        log.info("Asistencia actualizada id={}", asistenciaId);
        return toResponse(a);
    }

    @Transactional(readOnly = true)
    public AsistenciaResponse obtener(Long estudianteId, Long asistenciaId) {
        return asistenciaRepository.findByIdAndEstudianteId(asistenciaId, estudianteId)
                .map(this::toResponse)
                .orElseThrow(() -> new RecursoNotFoundException("Asistencia", asistenciaId));
    }

    public void eliminar(Long estudianteId, Long asistenciaId) {
        Asistencia a = asistenciaRepository.findByIdAndEstudianteId(asistenciaId, estudianteId)
                .orElseThrow(() -> new RecursoNotFoundException("Asistencia", asistenciaId));
        // Asistencia no extiende BaseEntity -> hard delete
        asistenciaRepository.delete(a);
        log.info("Asistencia eliminada id={}", asistenciaId);
    }

    // --------- helpers ---------

    private void verificarEstudianteExiste(Long estudianteId) {
        if (!estudianteRepository.findByIdAndDeletedAtIsNull(estudianteId).isPresent()) {
            throw new EstudianteNotFoundException(estudianteId);
        }
    }

    private AsistenciaResponse toResponse(Asistencia a) {
        return new AsistenciaResponse(a.getId(),
                a.getEstudiante().getId(),
                a.getAsignacionId(),
                a.getFechaClase(),
                a.getAsistio(),
                a.getJustificacion(),
                a.getObservaciones(),
                a.getCreatedAt(),
                a.getUpdatedAt());
    }
}
