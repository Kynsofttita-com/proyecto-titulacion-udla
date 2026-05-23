package com.escuela.cobros.service;

import com.escuela.cobros.dto.ReconciliacionListResponse;
import com.escuela.cobros.dto.ReconciliacionRequest;
import com.escuela.cobros.dto.ReconciliacionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Optional;

public interface ReconciliacionService {

    Page<ReconciliacionListResponse> findAll(Pageable pageable);

    ReconciliacionResponse findById(Long id);

    Optional<ReconciliacionResponse> findByFecha(LocalDate fecha);

    ReconciliacionResponse create(ReconciliacionRequest request);

    ReconciliacionResponse update(Long id, ReconciliacionRequest request);

    void delete(Long id);
}
