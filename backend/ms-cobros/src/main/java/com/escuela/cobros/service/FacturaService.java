package com.escuela.cobros.service;

import com.escuela.cobros.dto.FacturaListResponse;
import com.escuela.cobros.dto.FacturaRequest;
import com.escuela.cobros.dto.FacturaResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FacturaService {

    Page<FacturaListResponse> findAll(Pageable pageable);

    FacturaResponse findById(Long id);

    Page<FacturaListResponse> findByEstudianteId(Long estudianteId, Pageable pageable);

    FacturaResponse create(FacturaRequest request);

    FacturaResponse update(Long id, FacturaRequest request);

    void softDelete(Long id);
}
