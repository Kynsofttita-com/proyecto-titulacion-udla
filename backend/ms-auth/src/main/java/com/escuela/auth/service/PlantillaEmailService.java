package com.escuela.auth.service;

import com.escuela.auth.dto.CreatePlantillaEmailRequest;
import com.escuela.auth.dto.PlantillaEmailResponse;
import com.escuela.auth.dto.UpdatePlantillaEmailRequest;
import com.escuela.auth.entity.PlantillaEmail;
import com.escuela.auth.exception.DuplicateResourceException;
import com.escuela.auth.exception.ResourceNotFoundException;
import com.escuela.auth.mapper.PlantillaEmailMapper;
import com.escuela.auth.repository.PlantillaEmailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class PlantillaEmailService {

    private static final Logger log = LoggerFactory.getLogger(PlantillaEmailService.class);

    private final PlantillaEmailRepository repository;
    private final PlantillaEmailMapper mapper;

    public PlantillaEmailService(PlantillaEmailRepository repository, PlantillaEmailMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public Page<PlantillaEmailResponse> listar(Pageable pageable) {
        return repository.findByDeletedAtIsNull(pageable).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public PlantillaEmailResponse obtener(Long id) {
        PlantillaEmail entity = repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("PlantillaEmail", id));
        return mapper.toResponse(entity);
    }

    public PlantillaEmailResponse crear(CreatePlantillaEmailRequest request) {
        repository.findByCodigoAndDeletedAtIsNull(request.codigo()).ifPresent(c -> {
            throw new DuplicateResourceException("Ya existe plantilla con codigo " + request.codigo());
        });
        PlantillaEmail entity = mapper.toEntity(request);
        if (entity.getActiva() == null) entity.setActiva(Boolean.TRUE);
        entity = repository.save(entity);
        log.info("PlantillaEmail creada id={} codigo={}", entity.getId(), entity.getCodigo());
        return mapper.toResponse(entity);
    }

    public PlantillaEmailResponse actualizar(Long id, UpdatePlantillaEmailRequest request) {
        PlantillaEmail entity = repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("PlantillaEmail", id));
        if (request.codigo() != null && !request.codigo().equals(entity.getCodigo())) {
            repository.findByCodigoAndDeletedAtIsNull(request.codigo()).ifPresent(other -> {
                if (!other.getId().equals(id)) {
                    throw new DuplicateResourceException("Ya existe plantilla con codigo " + request.codigo());
                }
            });
        }
        mapper.updateEntity(request, entity);
        repository.save(entity);
        log.info("PlantillaEmail actualizada id={}", id);
        return mapper.toResponse(entity);
    }

    public void eliminar(Long id) {
        PlantillaEmail entity = repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("PlantillaEmail", id));
        entity.setDeletedAt(LocalDateTime.now());
        repository.save(entity);
        log.info("PlantillaEmail soft-deleted id={}", id);
    }
}
