package com.escuela.auth.service;

import com.escuela.auth.dto.CreateTipoCursoRequest;
import com.escuela.auth.dto.TipoCursoResponse;
import com.escuela.auth.dto.UpdateTipoCursoRequest;
import com.escuela.auth.entity.CategoriaLicencia;
import com.escuela.auth.entity.TipoCurso;
import com.escuela.auth.exception.DuplicateResourceException;
import com.escuela.auth.exception.ResourceNotFoundException;
import com.escuela.auth.mapper.TipoCursoMapper;
import com.escuela.auth.repository.CategoriaLicenciaRepository;
import com.escuela.auth.repository.TipoCursoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class TipoCursoService {

    private static final Logger log = LoggerFactory.getLogger(TipoCursoService.class);

    private final TipoCursoRepository repository;
    private final CategoriaLicenciaRepository categoriaRepository;
    private final TipoCursoMapper mapper;

    public TipoCursoService(TipoCursoRepository repository,
                            CategoriaLicenciaRepository categoriaRepository,
                            TipoCursoMapper mapper) {
        this.repository = repository;
        this.categoriaRepository = categoriaRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public Page<TipoCursoResponse> listar(Pageable pageable) {
        return repository.findByDeletedAtIsNull(pageable).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public TipoCursoResponse obtener(Long id) {
        TipoCurso entity = repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("TipoCurso", id));
        return mapper.toResponse(entity);
    }

    public TipoCursoResponse crear(CreateTipoCursoRequest request) {
        repository.findByNombreAndDeletedAtIsNull(request.nombre()).ifPresent(c -> {
            throw new DuplicateResourceException("Ya existe tipo de curso con nombre " + request.nombre());
        });

        TipoCurso entity = TipoCurso.builder()
                .nombre(request.nombre())
                .descripcion(request.descripcion())
                .duracionTotalHoras(request.duracionTotalHoras())
                .precioBase(request.precioBase())
                .activo(request.activo() == null ? Boolean.TRUE : request.activo())
                .build();

        if (request.categoriaLicenciaId() != null) {
            CategoriaLicencia cat = categoriaRepository.findByIdAndDeletedAtIsNull(request.categoriaLicenciaId())
                    .orElseThrow(() -> new ResourceNotFoundException("CategoriaLicencia", request.categoriaLicenciaId()));
            entity.setCategoriaLicencia(cat);
        }

        entity = repository.save(entity);
        log.info("TipoCurso creado id={} nombre={}", entity.getId(), entity.getNombre());
        return mapper.toResponse(entity);
    }

    public TipoCursoResponse actualizar(Long id, UpdateTipoCursoRequest request) {
        TipoCurso entity = repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("TipoCurso", id));

        if (request.nombre() != null && !request.nombre().equals(entity.getNombre())) {
            repository.findByNombreAndDeletedAtIsNull(request.nombre()).ifPresent(other -> {
                if (!other.getId().equals(id)) {
                    throw new DuplicateResourceException("Ya existe tipo de curso con nombre " + request.nombre());
                }
            });
            entity.setNombre(request.nombre());
        }
        if (request.descripcion() != null) entity.setDescripcion(request.descripcion());
        if (request.duracionTotalHoras() != null) entity.setDuracionTotalHoras(request.duracionTotalHoras());
        if (request.precioBase() != null) entity.setPrecioBase(request.precioBase());
        if (request.activo() != null) entity.setActivo(request.activo());
        if (request.categoriaLicenciaId() != null) {
            CategoriaLicencia cat = categoriaRepository.findByIdAndDeletedAtIsNull(request.categoriaLicenciaId())
                    .orElseThrow(() -> new ResourceNotFoundException("CategoriaLicencia", request.categoriaLicenciaId()));
            entity.setCategoriaLicencia(cat);
        }

        repository.save(entity);
        log.info("TipoCurso actualizado id={}", id);
        return mapper.toResponse(entity);
    }

    public void eliminar(Long id) {
        TipoCurso entity = repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("TipoCurso", id));
        entity.setDeletedAt(LocalDateTime.now());
        repository.save(entity);
        log.info("TipoCurso soft-deleted id={}", id);
    }
}
