package com.escuela.cobros.service;

import com.escuela.cobros.dto.CreateCobroRequest;
import com.escuela.cobros.dto.UpdateCobroRequest;
import com.escuela.cobros.dto.CobroListResponse;
import com.escuela.cobros.dto.CobroResponse;
import com.escuela.cobros.entity.Cobro;
import com.escuela.cobros.exception.CobroNotFoundException;
import com.escuela.cobros.mapper.CobroMapper;
import com.escuela.cobros.repository.CobroRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@Transactional
public class CobroServiceImpl implements CobroService {

    private final CobroRepository repository;
    private final CobroMapper mapper;

    public CobroServiceImpl(CobroRepository repository, CobroMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CobroListResponse> findAll(Pageable pageable, String estado) {
        if (estado != null && !estado.isBlank()) {
            return repository.findByEstadoAndDeletedAtIsNull(estado, pageable).map(mapper::toListResponse);
        }
        return repository.findByDeletedAtIsNull(pageable).map(mapper::toListResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CobroResponse findById(Long id) {
        Cobro cobro = repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new CobroNotFoundException(id));
        return mapper.toResponse(cobro);
    }

    @Override
    public CobroResponse create(CreateCobroRequest request) {
        Cobro cobro = mapper.toEntity(request);
        cobro.setEstado("REGISTRADO");
        cobro = repository.save(cobro);
        log.info("Cobro creado id={}", cobro.getId());
        return mapper.toResponse(cobro);
    }

    @Override
    public CobroResponse update(Long id, UpdateCobroRequest request) {
        Cobro cobro = repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new CobroNotFoundException(id));

        mapper.updateEntity(request, cobro);
        cobro.setUpdatedAt(LocalDateTime.now());
        cobro = repository.save(cobro);
        log.info("Cobro actualizado id={}", id);
        return mapper.toResponse(cobro);
    }

    @Override
    public void softDelete(Long id) {
        Cobro cobro = repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new CobroNotFoundException(id));
        cobro.setDeletedAt(LocalDateTime.now());
        repository.save(cobro);
        log.info("Cobro soft-deleted id={}", id);
    }
}
