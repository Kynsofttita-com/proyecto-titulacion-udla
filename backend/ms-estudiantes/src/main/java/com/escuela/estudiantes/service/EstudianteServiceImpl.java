package com.escuela.estudiantes.service;

import com.escuela.estudiantes.dto.CreateEstudianteRequest;
import com.escuela.estudiantes.dto.EstudianteDetailResponse;
import com.escuela.estudiantes.dto.EstudianteListResponse;
import com.escuela.estudiantes.dto.EstudianteResponse;
import com.escuela.estudiantes.dto.UpdateEstudianteRequest;
import com.escuela.estudiantes.entity.ContactoEmergencia;
import com.escuela.estudiantes.entity.Estudiante;
import com.escuela.estudiantes.exception.CedulaDuplicadaException;
import com.escuela.estudiantes.exception.CedulaInvalidaException;
import com.escuela.estudiantes.exception.EmailDuplicadoException;
import com.escuela.estudiantes.exception.EstudianteNotFoundException;
import com.escuela.estudiantes.mapper.EstudianteMapper;
import com.escuela.estudiantes.repository.EstudianteRepository;
import com.escuela.estudiantes.specification.EstudianteSpecifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementacion del servicio de Estudiantes.
 *
 * <p>Responsabilidades:</p>
 * <ul>
 *   <li>Validacion de cedula ecuatoriana (digito verificador via
 *       {@link CedulaValidator}).</li>
 *   <li>Validacion de unicidad de cedula y email (entre los no eliminados).</li>
 *   <li>Mapeo Entity &lt;-&gt; DTO via {@link EstudianteMapper}.</li>
 *   <li>Soft delete (no borra fisicamente).</li>
 * </ul>
 *
 * <p>NO publica eventos todavia (eso entra en PR #4 del Sprint 5).</p>
 */
@Service
public class EstudianteServiceImpl implements EstudianteService {

    private static final Logger log = LoggerFactory.getLogger(EstudianteServiceImpl.class);

    private final EstudianteRepository repository;
    private final EstudianteMapper mapper;

    public EstudianteServiceImpl(EstudianteRepository repository, EstudianteMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public EstudianteResponse create(CreateEstudianteRequest request) {
        validarCedula(request.cedula());
        if (repository.existsByCedulaAndDeletedAtIsNull(request.cedula())) {
            throw new CedulaDuplicadaException(request.cedula());
        }
        if (repository.existsByEmailAndDeletedAtIsNull(request.email())) {
            throw new EmailDuplicadoException(request.email());
        }

        Estudiante estudiante = mapper.toEntity(request);
        // Re-asignar el padre en los contactos hijos (MapStruct no lo hace solo
        // en relaciones bidireccionales)
        if (estudiante.getContactosEmergencia() != null) {
            for (ContactoEmergencia ce : estudiante.getContactosEmergencia()) {
                ce.setEstudiante(estudiante);
            }
        }

        Estudiante guardado = repository.save(estudiante);
        log.info("Estudiante creado id={} cedula={}", guardado.getId(), guardado.getCedula());
        return mapper.toResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public EstudianteDetailResponse findById(Long id) {
        Estudiante estudiante = repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new EstudianteNotFoundException(id));
        return mapper.toDetailResponse(estudiante);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EstudianteListResponse> findAll(Pageable pageable, String search, String estado) {
        Specification<Estudiante> spec = Specification.where(EstudianteSpecifications.notDeleted())
                .and(EstudianteSpecifications.estado(estado))
                .and(EstudianteSpecifications.searchTerm(search));
        return repository.findAll(spec, pageable).map(mapper::toListResponse);
    }

    @Override
    @Transactional
    public EstudianteResponse update(Long id, UpdateEstudianteRequest request) {
        Estudiante estudiante = repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new EstudianteNotFoundException(id));

        // Si cambian el email, validar unicidad (excepto si es el mismo email)
        if (request.email() != null && !request.email().equalsIgnoreCase(estudiante.getEmail())
                && repository.existsByEmailAndDeletedAtIsNull(request.email())) {
            throw new EmailDuplicadoException(request.email());
        }

        mapper.updateEntity(estudiante, request);
        Estudiante actualizado = repository.save(estudiante);
        log.info("Estudiante actualizado id={}", actualizado.getId());
        return mapper.toResponse(actualizado);
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        Estudiante estudiante = repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new EstudianteNotFoundException(id));
        estudiante.markAsDeleted();
        repository.save(estudiante);
        log.info("Estudiante soft-deleted id={}", id);
    }

    // -----------------------------------------------------------------------
    // helpers
    // -----------------------------------------------------------------------

    private void validarCedula(String cedula) {
        if (!CedulaValidator.isValid(cedula)) {
            throw new CedulaInvalidaException(cedula);
        }
    }
}
