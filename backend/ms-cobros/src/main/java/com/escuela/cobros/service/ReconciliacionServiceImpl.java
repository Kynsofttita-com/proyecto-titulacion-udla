package com.escuela.cobros.service;

import com.escuela.cobros.dto.ReconciliacionListResponse;
import com.escuela.cobros.dto.ReconciliacionRequest;
import com.escuela.cobros.dto.ReconciliacionResponse;
import com.escuela.cobros.entity.Reconciliacion;
import com.escuela.cobros.mapper.ReconciliacionMapper;
import com.escuela.cobros.repository.ReconciliacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ReconciliacionServiceImpl implements ReconciliacionService {

    private final ReconciliacionRepository reconciliacionRepository;
    private final ReconciliacionMapper reconciliacionMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<ReconciliacionListResponse> findAll(Pageable pageable) {
        log.debug("Buscando todas las reconciliaciones con paginación: {}", pageable);
        return reconciliacionRepository.findAllByOrderByFechaDesc(pageable)
            .map(reconciliacionMapper::toListResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ReconciliacionResponse findById(Long id) {
        log.debug("Buscando reconciliación con ID: {}", id);
        Reconciliacion reconciliacion = reconciliacionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Reconciliación con ID " + id + " no encontrada"));
        return reconciliacionMapper.toResponse(reconciliacion);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReconciliacionResponse> findByFecha(LocalDate fecha) {
        log.debug("Buscando reconciliación para fecha: {}", fecha);
        return reconciliacionRepository.findByFecha(fecha)
            .map(reconciliacionMapper::toResponse);
    }

    @Override
    public ReconciliacionResponse create(ReconciliacionRequest request) {
        log.info("Creando reconciliación para fecha: {}", request.fecha());

        if (reconciliacionRepository.findByFecha(request.fecha()).isPresent()) {
            log.warn("Ya existe reconciliación para la fecha: {}", request.fecha());
            throw new RuntimeException("Ya existe reconciliación para la fecha " + request.fecha());
        }

        Reconciliacion reconciliacion = reconciliacionMapper.toEntity(request);
        Reconciliacion guardada = reconciliacionRepository.save(reconciliacion);

        log.info("Reconciliación creada con ID: {}", guardada.getId());
        return reconciliacionMapper.toResponse(guardada);
    }

    @Override
    public ReconciliacionResponse update(Long id, ReconciliacionRequest request) {
        log.info("Actualizando reconciliación con ID: {}", id);

        Reconciliacion reconciliacion = reconciliacionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Reconciliación con ID " + id + " no encontrada"));

        reconciliacionMapper.updateEntity(request, reconciliacion);
        Reconciliacion actualizada = reconciliacionRepository.save(reconciliacion);

        log.info("Reconciliación actualizada con ID: {}", id);
        return reconciliacionMapper.toResponse(actualizada);
    }

    @Override
    public void delete(Long id) {
        log.info("Eliminando reconciliación con ID: {}", id);

        if (!reconciliacionRepository.existsById(id)) {
            throw new RuntimeException("Reconciliación con ID " + id + " no encontrada");
        }

        reconciliacionRepository.deleteById(id);
        log.info("Reconciliación eliminada con ID: {}", id);
    }
}
