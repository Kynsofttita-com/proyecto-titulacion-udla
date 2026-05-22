package com.escuela.auth.service;

import com.escuela.auth.dto.CategoriaLicenciaResponse;
import com.escuela.auth.dto.CreateCategoriaLicenciaRequest;
import com.escuela.auth.dto.UpdateCategoriaLicenciaRequest;
import com.escuela.auth.entity.CategoriaLicencia;
import com.escuela.auth.exception.DuplicateResourceException;
import com.escuela.auth.exception.ResourceNotFoundException;
import com.escuela.auth.mapper.CategoriaLicenciaMapper;
import com.escuela.auth.repository.CategoriaLicenciaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class CategoriaLicenciaService {

    private static final Logger log = LoggerFactory.getLogger(CategoriaLicenciaService.class);

    private final CategoriaLicenciaRepository repository;
    private final CategoriaLicenciaMapper mapper;

    public CategoriaLicenciaService(CategoriaLicenciaRepository repository,
                                    CategoriaLicenciaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public Page<CategoriaLicenciaResponse> listar(Pageable pageable) {
        return repository.findByDeletedAtIsNull(pageable).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public CategoriaLicenciaResponse obtener(Long id) {
        CategoriaLicencia cat = repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("CategoriaLicencia", id));
        return mapper.toResponse(cat);
    }

    public CategoriaLicenciaResponse crear(CreateCategoriaLicenciaRequest request) {
        repository.findByCodigoAndDeletedAtIsNull(request.codigo()).ifPresent(c -> {
            throw new DuplicateResourceException("Ya existe categoria con codigo " + request.codigo());
        });
        CategoriaLicencia entity = mapper.toEntity(request);
        if (entity.getActiva() == null) entity.setActiva(Boolean.TRUE);
        entity = repository.save(entity);
        log.info("CategoriaLicencia creada id={} codigo={}", entity.getId(), entity.getCodigo());
        return mapper.toResponse(entity);
    }

    public CategoriaLicenciaResponse actualizar(Long id, UpdateCategoriaLicenciaRequest request) {
        CategoriaLicencia cat = repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("CategoriaLicencia", id));
        if (request.codigo() != null && !request.codigo().equals(cat.getCodigo())) {
            repository.findByCodigoAndDeletedAtIsNull(request.codigo()).ifPresent(other -> {
                if (!other.getId().equals(id)) {
                    throw new DuplicateResourceException("Ya existe categoria con codigo " + request.codigo());
                }
            });
        }
        mapper.updateEntity(request, cat);
        repository.save(cat);
        log.info("CategoriaLicencia actualizada id={}", id);
        return mapper.toResponse(cat);
    }

    public void eliminar(Long id) {
        CategoriaLicencia cat = repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("CategoriaLicencia", id));
        cat.setDeletedAt(LocalDateTime.now());
        repository.save(cat);
        log.info("CategoriaLicencia soft-deleted id={}", id);
    }
}
