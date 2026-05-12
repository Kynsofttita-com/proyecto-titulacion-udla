package com.escuela.asignaciones.service;

import com.escuela.asignaciones.dto.CreateAsignacionRequest;
import com.escuela.asignaciones.dto.UpdateAsignacionRequest;
import com.escuela.asignaciones.dto.AsignacionListResponse;
import com.escuela.asignaciones.dto.AsignacionResponse;
import com.escuela.asignaciones.entity.Asignacion;
import com.escuela.asignaciones.exception.AsignacionNotFoundException;
import com.escuela.asignaciones.exception.DisponibilidadException;
import com.escuela.asignaciones.mapper.AsignacionMapper;
import com.escuela.asignaciones.repository.AsignacionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@Transactional
public class AsignacionServiceImpl implements AsignacionService {

    private final AsignacionRepository repository;
    private final AsignacionMapper mapper;

    public AsignacionServiceImpl(AsignacionRepository repository, AsignacionMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AsignacionListResponse> findAll(Pageable pageable) {
        return repository.findByDeletedAtIsNull(pageable).map(mapper::toListResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AsignacionResponse findById(Long id) {
        Asignacion asignacion = repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AsignacionNotFoundException(id));
        return mapper.toResponse(asignacion);
    }

    @Override
    public AsignacionResponse create(CreateAsignacionRequest request) {
        validarDisponibilidad(request);

        Asignacion asignacion = mapper.toEntity(request);
        asignacion.setEstado("CONFIRMADA");
        asignacion = repository.save(asignacion);
        log.info("Asignación creada id={}", asignacion.getId());
        return mapper.toResponse(asignacion);
    }

    @Override
    public AsignacionResponse update(Long id, UpdateAsignacionRequest request) {
        Asignacion asignacion = repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AsignacionNotFoundException(id));

        mapper.updateEntity(request, asignacion);
        asignacion.setUpdatedAt(LocalDateTime.now());
        asignacion = repository.save(asignacion);
        log.info("Asignación actualizada id={}", id);
        return mapper.toResponse(asignacion);
    }

    @Override
    public void softDelete(Long id) {
        Asignacion asignacion = repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AsignacionNotFoundException(id));
        asignacion.setDeletedAt(LocalDateTime.now());
        repository.save(asignacion);
        log.info("Asignación soft-deleted id={}", id);
    }

    private void validarDisponibilidad(CreateAsignacionRequest request) {
        long conflictosInstructor = repository.countByInstructorIdAndFechaAndEstadoAndDeletedAtIsNull(
                request.instructorId(), request.fecha(), "CONFIRMADA"
        );
        if (conflictosInstructor > 0) {
            throw new DisponibilidadException("Instructor no disponible en esa fecha y hora");
        }

        long conflictosVehiculo = repository.countByVehiculoIdAndFechaAndEstadoAndDeletedAtIsNull(
                request.vehiculoId(), request.fecha(), "CONFIRMADA"
        );
        if (conflictosVehiculo > 0) {
            throw new DisponibilidadException("Vehículo no disponible en esa fecha y hora");
        }
    }
}
