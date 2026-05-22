package com.escuela.instructores.service;

import com.escuela.instructores.dto.CertificacionRequest;
import com.escuela.instructores.dto.CertificacionResponse;
import com.escuela.instructores.entity.Certificacion;
import com.escuela.instructores.entity.Instructor;
import com.escuela.instructores.exception.RecursoNotFoundException;
import com.escuela.instructores.repository.CertificacionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class CertificacionService {

    private static final Logger log = LoggerFactory.getLogger(CertificacionService.class);

    private final CertificacionRepository repository;
    private final InstructorService instructorService;

    public CertificacionService(CertificacionRepository repository, InstructorService instructorService) {
        this.repository = repository;
        this.instructorService = instructorService;
    }

    @Transactional(readOnly = true)
    public List<CertificacionResponse> listar(Long instructorId) {
        instructorService.buscarOFallar(instructorId);
        return repository.findByInstructorIdAndDeletedAtIsNullOrderByFechaObtencionDesc(instructorId)
                .stream().map(this::toResponse).toList();
    }

    public CertificacionResponse agregar(Long instructorId, CertificacionRequest request) {
        Instructor instructor = instructorService.buscarOFallar(instructorId);
        Certificacion c = Certificacion.builder()
                .instructor(instructor)
                .tipo(request.tipo())
                .fechaObtencion(request.fechaObtencion())
                .vigenciaHasta(request.vigenciaHasta())
                .entidadEmisora(request.entidadEmisora())
                .archivoUrl(request.archivoUrl())
                .observaciones(request.observaciones())
                .build();
        c = repository.save(c);
        log.info("Certificacion agregada id={} instructorId={} tipo={}",
                c.getId(), instructorId, c.getTipo());
        return toResponse(c);
    }

    public CertificacionResponse actualizar(Long instructorId, Long certId, CertificacionRequest request) {
        Certificacion c = repository.findByIdAndInstructorIdAndDeletedAtIsNull(certId, instructorId)
                .orElseThrow(() -> new RecursoNotFoundException("Certificacion", certId));
        c.setTipo(request.tipo());
        c.setFechaObtencion(request.fechaObtencion());
        c.setVigenciaHasta(request.vigenciaHasta());
        c.setEntidadEmisora(request.entidadEmisora());
        c.setArchivoUrl(request.archivoUrl());
        c.setObservaciones(request.observaciones());
        repository.save(c);
        log.info("Certificacion actualizada id={}", certId);
        return toResponse(c);
    }

    public void eliminar(Long instructorId, Long certId) {
        Certificacion c = repository.findByIdAndInstructorIdAndDeletedAtIsNull(certId, instructorId)
                .orElseThrow(() -> new RecursoNotFoundException("Certificacion", certId));
        c.setDeletedAt(LocalDateTime.now());
        repository.save(c);
        log.info("Certificacion soft-deleted id={}", certId);
    }

    private CertificacionResponse toResponse(Certificacion c) {
        return new CertificacionResponse(c.getId(), c.getTipo(),
                c.getFechaObtencion(), c.getVigenciaHasta(),
                c.getEntidadEmisora(), c.getArchivoUrl(), c.getObservaciones());
    }
}
