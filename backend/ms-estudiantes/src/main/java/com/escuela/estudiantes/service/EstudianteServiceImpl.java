package com.escuela.estudiantes.service;

import com.escuela.common.validation.core.CedulaEcuadorValidator;
import com.escuela.common.events.estudiantes.EstudianteActualizadoEvent;
import com.escuela.common.events.estudiantes.EstudianteCreadoEvent;
import com.escuela.common.events.estudiantes.EstudianteEliminadoEvent;
import com.escuela.common.events.estudiantes.EstudianteMatriculadoEvent;
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
import com.escuela.estudiantes.mapper.EstudianteMapperImpl;
import com.escuela.estudiantes.repository.EstudianteRepository;
import com.escuela.estudiantes.specification.EstudianteSpecifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Implementacion del servicio de Estudiantes.
 *
 * <p>Responsabilidades:</p>
 * <ul>
 *   <li>Validacion de cedula ecuatoriana (digito verificador via
 *       {@link CedulaEcuadorValidator}).</li>
 *   <li>Validacion de unicidad de cedula y email (entre los no eliminados).</li>
 *   <li>Mapeo Entity &lt;-&gt; DTO via {@link EstudianteMapper}.</li>
 *   <li>Soft delete (no borra fisicamente).</li>
 *   <li>Publicacion de eventos a RabbitMQ via {@link EstudianteEventDispatcher}.</li>
 * </ul>
 *
 * <p>Eventos publicados:</p>
 * <ul>
 *   <li>{@code estudiantes.creado} - tras crear un estudiante</li>
 *   <li>{@code estudiantes.actualizado} - tras actualizar</li>
 *   <li>{@code estudiantes.eliminado} - tras soft-delete</li>
 *   <li>{@code estudiantes.matriculado} - cuando el estado pasa a ACTIVO</li>
 * </ul>
 */
@Service
public class EstudianteServiceImpl implements EstudianteService {

    private static final Logger log = LoggerFactory.getLogger(EstudianteServiceImpl.class);

    private static final String ESTADO_ACTIVO = "ACTIVO";

    private final EstudianteRepository repository;
    private final EstudianteEventDispatcher eventDispatcher;
    private EstudianteMapper mapper;

    public EstudianteServiceImpl(EstudianteRepository repository,
                                 EstudianteEventDispatcher eventDispatcher) {
        this.repository = repository;
        this.eventDispatcher = eventDispatcher;
    }

    private EstudianteMapper getMapper() {
        if (mapper == null) {
            this.mapper = new EstudianteMapperImpl();
        }
        return mapper;
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

        Estudiante estudiante = getMapper().toEntity(request);
        // Re-asignar el padre en los contactos hijos (MapStruct no lo hace solo
        // en relaciones bidireccionales)
        if (estudiante.getContactosEmergencia() != null) {
            for (ContactoEmergencia ce : estudiante.getContactosEmergencia()) {
                ce.setEstudiante(estudiante);
            }
        }

        Estudiante guardado = repository.save(estudiante);
        log.info("Estudiante creado id={} cedula={}", guardado.getId(), guardado.getCedula());

        eventDispatcher.publishCreado(EstudianteCreadoEvent.builder()
                .estudianteId(guardado.getId())
                .cedula(guardado.getCedula())
                .email(guardado.getEmail())
                .nombreCompleto(nombreCompleto(guardado))
                .estado(guardado.getEstado())
                .build());

        return getMapper().toResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public EstudianteDetailResponse findById(Long id) {
        Estudiante estudiante = repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new EstudianteNotFoundException(id));
        return getMapper().toDetailResponse(estudiante);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EstudianteListResponse> findAll(Pageable pageable, String search, String estado) {
        Specification<Estudiante> spec = Specification.where(EstudianteSpecifications.notDeleted())
                .and(EstudianteSpecifications.estado(estado))
                .and(EstudianteSpecifications.searchTerm(search));
        return repository.findAll(spec, pageable).map(getMapper()::toListResponse);
    }

    @Override
    @Transactional
    public EstudianteResponse update(Long id, UpdateEstudianteRequest request) {
        Estudiante estudiante = repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new EstudianteNotFoundException(id));

        // Capturar estado previo para detectar transicion a "matriculado"
        String estadoPrevio = estudiante.getEstado();

        // Si cambian el email, validar unicidad (excepto si es el mismo email)
        if (request.email() != null && !request.email().equalsIgnoreCase(estudiante.getEmail())
                && repository.existsByEmailAndDeletedAtIsNull(request.email())) {
            throw new EmailDuplicadoException(request.email());
        }

        getMapper().updateEntity(estudiante, request);
        Estudiante actualizado = repository.save(estudiante);
        log.info("Estudiante actualizado id={}", actualizado.getId());

        eventDispatcher.publishActualizado(EstudianteActualizadoEvent.builder()
                .estudianteId(actualizado.getId())
                .cedula(actualizado.getCedula())
                .email(actualizado.getEmail())
                .nombreCompleto(nombreCompleto(actualizado))
                .estado(actualizado.getEstado())
                .build());

        // Si el estado pasa a ACTIVO, ademas disparamos el evento de matricula
        if (!Objects.equals(estadoPrevio, ESTADO_ACTIVO)
                && ESTADO_ACTIVO.equals(actualizado.getEstado())) {
            eventDispatcher.publishMatriculado(EstudianteMatriculadoEvent.builder()
                    .estudianteId(actualizado.getId())
                    .cedula(actualizado.getCedula())
                    .email(actualizado.getEmail())
                    .nombreCompleto(nombreCompleto(actualizado))
                    .fechaMatricula(actualizado.getFechaMatricula())
                    .build());
        }

        return getMapper().toResponse(actualizado);
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        Estudiante estudiante = repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new EstudianteNotFoundException(id));
        estudiante.markAsDeleted();
        repository.save(estudiante);
        log.info("Estudiante soft-deleted id={}", id);

        eventDispatcher.publishEliminado(EstudianteEliminadoEvent.builder()
                .estudianteId(estudiante.getId())
                .cedula(estudiante.getCedula())
                .build());
    }

    // -----------------------------------------------------------------------
    // helpers
    // -----------------------------------------------------------------------

    private void validarCedula(String cedula) {
        if (!CedulaEcuadorValidator.isValid(cedula)) {
            throw new CedulaInvalidaException(cedula);
        }
    }

    private String nombreCompleto(Estudiante e) {
        return e.getNombre() + " " + e.getApellido();
    }
}
