package com.escuela.cobros.service;

import com.escuela.cobros.dto.CreateCobroRequest;
import com.escuela.cobros.dto.UpdateCobroRequest;
import com.escuela.cobros.dto.CobroListResponse;
import com.escuela.cobros.dto.CobroResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CobroService {
    Page<CobroListResponse> findAll(Pageable pageable, String estado);
    CobroResponse findById(Long id);
    CobroResponse create(CreateCobroRequest request);
    CobroResponse update(Long id, UpdateCobroRequest request);
    void softDelete(Long id);
}
