package com.escuela.estudiantes.service;

import com.escuela.estudiantes.dto.ContactoEmergenciaRequest;
import com.escuela.estudiantes.dto.ContactoEmergenciaResponse;
import com.escuela.estudiantes.entity.ContactoEmergencia;
import com.escuela.estudiantes.entity.Estudiante;
import com.escuela.estudiantes.exception.EstudianteNotFoundException;
import com.escuela.estudiantes.exception.RecursoNotFoundException;
import com.escuela.estudiantes.repository.ContactoEmergenciaRepository;
import com.escuela.estudiantes.repository.EstudianteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ContactoEmergenciaService {

    private static final Logger log = LoggerFactory.getLogger(ContactoEmergenciaService.class);

    private final ContactoEmergenciaRepository contactoRepository;
    private final EstudianteRepository estudianteRepository;

    public ContactoEmergenciaService(ContactoEmergenciaRepository contactoRepository,
                                     EstudianteRepository estudianteRepository) {
        this.contactoRepository = contactoRepository;
        this.estudianteRepository = estudianteRepository;
    }

    @Transactional(readOnly = true)
    public List<ContactoEmergenciaResponse> listar(Long estudianteId) {
        verificarEstudianteExiste(estudianteId);
        return contactoRepository.findByEstudianteIdAndDeletedAtIsNull(estudianteId)
                .stream().map(this::toResponse).toList();
    }

    public ContactoEmergenciaResponse agregar(Long estudianteId, ContactoEmergenciaRequest request) {
        Estudiante estudiante = estudianteRepository.findByIdAndDeletedAtIsNull(estudianteId)
                .orElseThrow(() -> new EstudianteNotFoundException(estudianteId));

        boolean esPrincipal = Boolean.TRUE.equals(request.esPrincipal());
        if (esPrincipal) {
            despromoverPrincipales(estudianteId);
        }

        ContactoEmergencia c = ContactoEmergencia.builder()
                .estudiante(estudiante)
                .nombre(request.nombre())
                .telefono(request.telefono())
                .parentesco(request.parentesco())
                .esPrincipal(esPrincipal)
                .build();

        c = contactoRepository.save(c);
        log.info("ContactoEmergencia agregado id={} estudianteId={} principal={}",
                c.getId(), estudianteId, esPrincipal);
        return toResponse(c);
    }

    public ContactoEmergenciaResponse actualizar(Long estudianteId, Long contactoId,
                                                 ContactoEmergenciaRequest request) {
        ContactoEmergencia c = contactoRepository.findByIdAndEstudianteIdAndDeletedAtIsNull(contactoId, estudianteId)
                .orElseThrow(() -> new RecursoNotFoundException("ContactoEmergencia", contactoId));

        c.setNombre(request.nombre());
        c.setTelefono(request.telefono());
        c.setParentesco(request.parentesco());

        boolean nuevoPrincipal = Boolean.TRUE.equals(request.esPrincipal());
        if (nuevoPrincipal && !Boolean.TRUE.equals(c.getEsPrincipal())) {
            despromoverPrincipales(estudianteId);
        }
        c.setEsPrincipal(nuevoPrincipal);

        contactoRepository.save(c);
        log.info("ContactoEmergencia actualizado id={}", contactoId);
        return toResponse(c);
    }

    public void eliminar(Long estudianteId, Long contactoId) {
        ContactoEmergencia c = contactoRepository.findByIdAndEstudianteIdAndDeletedAtIsNull(contactoId, estudianteId)
                .orElseThrow(() -> new RecursoNotFoundException("ContactoEmergencia", contactoId));
        c.setDeletedAt(LocalDateTime.now());
        contactoRepository.save(c);
        log.info("ContactoEmergencia soft-deleted id={}", contactoId);
    }

    // --------- helpers ---------

    private void despromoverPrincipales(Long estudianteId) {
        contactoRepository.findByEstudianteIdAndDeletedAtIsNull(estudianteId).stream()
                .filter(c -> Boolean.TRUE.equals(c.getEsPrincipal()))
                .forEach(c -> {
                    c.setEsPrincipal(Boolean.FALSE);
                    contactoRepository.save(c);
                });
    }

    private void verificarEstudianteExiste(Long estudianteId) {
        if (!estudianteRepository.findByIdAndDeletedAtIsNull(estudianteId).isPresent()) {
            throw new EstudianteNotFoundException(estudianteId);
        }
    }

    private ContactoEmergenciaResponse toResponse(ContactoEmergencia c) {
        return new ContactoEmergenciaResponse(c.getId(), c.getNombre(),
                c.getTelefono(), c.getParentesco(), c.getEsPrincipal());
    }
}
