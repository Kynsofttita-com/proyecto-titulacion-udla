package com.escuela.instructores.service;

import com.escuela.instructores.dto.DisponibilidadDelDiaResponse;
import com.escuela.instructores.dto.DisponibilidadRequest;
import com.escuela.instructores.dto.DisponibilidadResponse;
import com.escuela.instructores.entity.Disponibilidad;
import com.escuela.instructores.entity.HorarioTrabajo;
import com.escuela.instructores.entity.Instructor;
import com.escuela.instructores.exception.RecursoNotFoundException;
import com.escuela.instructores.repository.DisponibilidadRepository;
import com.escuela.instructores.repository.HorarioTrabajoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class DisponibilidadService {

    private static final Logger log = LoggerFactory.getLogger(DisponibilidadService.class);

    private final DisponibilidadRepository disponibilidadRepository;
    private final HorarioTrabajoRepository horarioRepository;
    private final InstructorService instructorService;

    public DisponibilidadService(DisponibilidadRepository disponibilidadRepository,
                                  HorarioTrabajoRepository horarioRepository,
                                  InstructorService instructorService) {
        this.disponibilidadRepository = disponibilidadRepository;
        this.horarioRepository = horarioRepository;
        this.instructorService = instructorService;
    }

    @Transactional(readOnly = true)
    public List<DisponibilidadResponse> listar(Long instructorId) {
        instructorService.buscarOFallar(instructorId);
        return disponibilidadRepository
                .findByInstructorIdAndDeletedAtIsNullOrderByDiaSemanaAscHoraInicioAsc(instructorId)
                .stream().map(this::toResponse).toList();
    }

    public DisponibilidadResponse agregar(Long instructorId, DisponibilidadRequest request) {
        Instructor instructor = instructorService.buscarOFallar(instructorId);
        if (request.horaInicio().isAfter(request.horaFin())) {
            throw new IllegalArgumentException("horaInicio debe ser anterior a horaFin");
        }
        Disponibilidad d = Disponibilidad.builder()
                .instructor(instructor)
                .diaSemana(request.diaSemana())
                .horaInicio(request.horaInicio())
                .horaFin(request.horaFin())
                .build();
        d = disponibilidadRepository.save(d);
        log.info("Disponibilidad agregada id={} instructorId={} dia={}",
                d.getId(), instructorId, d.getDiaSemana());
        return toResponse(d);
    }

    public DisponibilidadResponse actualizar(Long instructorId, Long disponibilidadId,
                                             DisponibilidadRequest request) {
        Disponibilidad d = disponibilidadRepository
                .findByIdAndInstructorIdAndDeletedAtIsNull(disponibilidadId, instructorId)
                .orElseThrow(() -> new RecursoNotFoundException("Disponibilidad", disponibilidadId));
        if (request.horaInicio().isAfter(request.horaFin())) {
            throw new IllegalArgumentException("horaInicio debe ser anterior a horaFin");
        }
        d.setDiaSemana(request.diaSemana());
        d.setHoraInicio(request.horaInicio());
        d.setHoraFin(request.horaFin());
        disponibilidadRepository.save(d);
        log.info("Disponibilidad actualizada id={}", disponibilidadId);
        return toResponse(d);
    }

    public void eliminar(Long instructorId, Long disponibilidadId) {
        Disponibilidad d = disponibilidadRepository
                .findByIdAndInstructorIdAndDeletedAtIsNull(disponibilidadId, instructorId)
                .orElseThrow(() -> new RecursoNotFoundException("Disponibilidad", disponibilidadId));
        d.setDeletedAt(LocalDateTime.now());
        disponibilidadRepository.save(d);
        log.info("Disponibilidad soft-deleted id={}", disponibilidadId);
    }

    /**
     * Calcula la disponibilidad de un instructor en una fecha concreta.
     * Combina disponibilidad semanal recurrente con excepciones puntuales
     * (HorarioTrabajo: EXTRA agrega franjas, AUSENCIA bloquea el dia).
     */
    @Transactional(readOnly = true)
    public DisponibilidadDelDiaResponse disponibilidadDelDia(Long instructorId, LocalDate fecha) {
        instructorService.buscarOFallar(instructorId);
        short dia = (short) fecha.getDayOfWeek().getValue();

        List<HorarioTrabajo> excepciones =
                horarioRepository.findByInstructorIdAndFechaAndDeletedAtIsNull(instructorId, fecha);
        boolean ausenciaTotal = excepciones.stream()
                .anyMatch(h -> "AUSENCIA".equalsIgnoreCase(h.getTipo())
                        && (h.getHoraInicio() == null || h.getHoraFin() == null));
        if (ausenciaTotal) {
            return new DisponibilidadDelDiaResponse(instructorId, fecha, dia, false,
                    "Ausencia registrada para esta fecha", List.of());
        }

        List<DisponibilidadDelDiaResponse.FranjaHoraria> franjas = new ArrayList<>();
        disponibilidadRepository
                .findByInstructorIdAndDiaSemanaAndDeletedAtIsNullOrderByHoraInicioAsc(instructorId, dia)
                .forEach(d -> franjas.add(
                        new DisponibilidadDelDiaResponse.FranjaHoraria(d.getHoraInicio(), d.getHoraFin())));

        excepciones.stream()
                .filter(h -> "EXTRA".equalsIgnoreCase(h.getTipo())
                        && h.getHoraInicio() != null && h.getHoraFin() != null)
                .forEach(h -> franjas.add(
                        new DisponibilidadDelDiaResponse.FranjaHoraria(h.getHoraInicio(), h.getHoraFin())));

        boolean disponible = !franjas.isEmpty();
        return new DisponibilidadDelDiaResponse(instructorId, fecha, dia,
                disponible,
                disponible ? null : "Sin franjas disponibles para este dia",
                franjas);
    }

    private DisponibilidadResponse toResponse(Disponibilidad d) {
        return new DisponibilidadResponse(d.getId(), d.getDiaSemana(),
                d.getHoraInicio(), d.getHoraFin());
    }
}
