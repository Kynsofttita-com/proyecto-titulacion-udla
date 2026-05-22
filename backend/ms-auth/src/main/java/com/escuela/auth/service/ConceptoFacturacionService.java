package com.escuela.auth.service;

import com.escuela.auth.dto.ConceptoFacturacionResponse;
import com.escuela.auth.dto.CreateConceptoFacturacionRequest;
import com.escuela.auth.dto.UpdateConceptoFacturacionRequest;
import com.escuela.auth.entity.ConceptoFacturacion;
import com.escuela.auth.exception.DuplicateResourceException;
import com.escuela.auth.exception.ResourceNotFoundException;
import com.escuela.auth.mapper.ConceptoFacturacionMapper;
import com.escuela.auth.repository.ConceptoFacturacionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class ConceptoFacturacionService {

    private static final Logger log = LoggerFactory.getLogger(ConceptoFacturacionService.class);

    private final ConceptoFacturacionRepository repository;
    private final ConceptoFacturacionMapper mapper;

    public ConceptoFacturacionService(ConceptoFacturacionRepository repository,
                                      ConceptoFacturacionMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public Page<ConceptoFacturacionResponse> listar(Pageable pageable) {
        return repository.findByDeletedAtIsNull(pageable).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ConceptoFacturacionResponse obtener(Long id) {
        ConceptoFacturacion entity = repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("ConceptoFacturacion", id));
        return mapper.toResponse(entity);
    }

    public ConceptoFacturacionResponse crear(CreateConceptoFacturacionRequest request) {
        repository.findByNombreAndDeletedAtIsNull(request.nombre()).ifPresent(c -> {
            throw new DuplicateResourceException("Ya existe concepto con nombre " + request.nombre());
        });
        ConceptoFacturacion entity = mapper.toEntity(request);
        if (entity.getActivo() == null) entity.setActivo(Boolean.TRUE);
        entity = repository.save(entity);
        log.info("ConceptoFacturacion creado id={} nombre={}", entity.getId(), entity.getNombre());
        return mapper.toResponse(entity);
    }

    public ConceptoFacturacionResponse actualizar(Long id, UpdateConceptoFacturacionRequest request) {
        ConceptoFacturacion entity = repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("ConceptoFacturacion", id));
        if (request.nombre() != null && !request.nombre().equals(entity.getNombre())) {
            repository.findByNombreAndDeletedAtIsNull(request.nombre()).ifPresent(other -> {
                if (!other.getId().equals(id)) {
                    throw new DuplicateResourceException("Ya existe concepto con nombre " + request.nombre());
                }
            });
        }
        mapper.updateEntity(request, entity);
        repository.save(entity);
        log.info("ConceptoFacturacion actualizado id={}", id);
        return mapper.toResponse(entity);
    }

    public void eliminar(Long id) {
        ConceptoFacturacion entity = repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("ConceptoFacturacion", id));
        entity.setDeletedAt(LocalDateTime.now());
        repository.save(entity);
        log.info("ConceptoFacturacion soft-deleted id={}", id);
    }
}
