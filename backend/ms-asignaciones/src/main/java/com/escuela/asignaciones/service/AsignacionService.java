package com.escuela.asignaciones.service;

import com.escuela.asignaciones.dto.CreateAsignacionRequest;
import com.escuela.asignaciones.dto.UpdateAsignacionRequest;
import com.escuela.asignaciones.dto.AsignacionListResponse;
import com.escuela.asignaciones.dto.AsignacionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AsignacionService {
    Page<AsignacionListResponse> findAll(Pageable pageable);
    AsignacionResponse findById(Long id);
    AsignacionResponse create(CreateAsignacionRequest request);
    AsignacionResponse update(Long id, UpdateAsignacionRequest request);
    void softDelete(Long id);
}
