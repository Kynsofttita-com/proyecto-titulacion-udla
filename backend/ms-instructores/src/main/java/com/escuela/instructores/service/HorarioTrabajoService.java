package com.escuela.instructores.service;

import com.escuela.instructores.dto.HorarioTrabajoRequest;
import com.escuela.instructores.dto.HorarioTrabajoResponse;
import com.escuela.instructores.entity.HorarioTrabajo;
import com.escuela.instructores.entity.Instructor;
import com.escuela.instructores.exception.RecursoNotFoundException;
import com.escuela.instructores.repository.HorarioTrabajoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class HorarioTrabajoService {

    private static final Logger log = LoggerFactory.getLogger(HorarioTrabajoService.class);

    private final HorarioTrabajoRepository repository;
    private final InstructorService instructorService;

    public HorarioTrabajoService(HorarioTrabajoRepository repository, InstructorService instructorService) {
        this.repository = repository;
        this.instructorService = instructorService;
    }

    @Transactional(readOnly = true)
    public List<HorarioTrabajoResponse> listar(Long instructorId) {
        instructorService.buscarOFallar(instructorId);
        return repository.findByInstructorIdAndDeletedAtIsNullOrderByFechaDesc(instructorId)
                .stream().map(this::toResponse).toList();
    }

    public HorarioTrabajoResponse agregar(Long instructorId, HorarioTrabajoRequest request) {
        Instructor instructor = instructorService.buscarOFallar(instructorId);

        // Para EXTRA, hora_inicio y hora_fin son obligatorias
        if ("EXTRA".equalsIgnoreCase(request.tipo())
                && (request.horaInicio() == null || request.horaFin() == null)) {
            throw new IllegalArgumentException("Para tipo EXTRA se requieren horaInicio y horaFin");
        }
        if (request.horaInicio() != null && request.horaFin() != null
                && request.horaInicio().isAfter(request.horaFin())) {
            throw new IllegalArgumentException("horaInicio debe ser anterior a horaFin");
        }

        HorarioTrabajo h = HorarioTrabajo.builder()
                .instructor(instructor)
                .fecha(request.fecha())
                .horaInicio(request.horaInicio())
                .horaFin(request.horaFin())
                .tipo(request.tipo().toUpperCase())
                .motivo(request.motivo())
                .build();
        h = repository.save(h);
        log.info("HorarioTrabajo agregado id={} instructorId={} fecha={} tipo={}",
                h.getId(), instructorId, h.getFecha(), h.getTipo());
        return toResponse(h);
    }

    public void eliminar(Long instructorId, Long horarioId) {
        HorarioTrabajo h = repository.findByIdAndInstructorIdAndDeletedAtIsNull(horarioId, instructorId)
                .orElseThrow(() -> new RecursoNotFoundException("HorarioTrabajo", horarioId));
        h.setDeletedAt(LocalDateTime.now());
        repository.save(h);
        log.info("HorarioTrabajo soft-deleted id={}", horarioId);
    }

    private HorarioTrabajoResponse toResponse(HorarioTrabajo h) {
        return new HorarioTrabajoResponse(h.getId(), h.getFecha(),
                h.getHoraInicio(), h.getHoraFin(), h.getTipo(), h.getMotivo());
    }
}
