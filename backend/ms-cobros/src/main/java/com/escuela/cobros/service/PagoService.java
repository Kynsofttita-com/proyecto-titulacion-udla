package com.escuela.cobros.service;

import com.escuela.cobros.dto.PagoListResponse;
import com.escuela.cobros.dto.PagoRequest;
import com.escuela.cobros.dto.PagoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PagoService {

    Page<PagoListResponse> findAll(Pageable pageable);

    PagoResponse findById(Long id);

    Page<PagoListResponse> findByEstudianteId(Long estudianteId, Pageable pageable);

    Page<PagoListResponse> findByFacturaId(Long facturaId, Pageable pageable);

    PagoResponse create(PagoRequest request);
}
