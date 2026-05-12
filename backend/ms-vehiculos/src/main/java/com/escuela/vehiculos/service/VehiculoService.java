package com.escuela.vehiculos.service;

import com.escuela.vehiculos.dto.CreateVehiculoRequest;
import com.escuela.vehiculos.dto.UpdateVehiculoRequest;
import com.escuela.vehiculos.dto.VehiculoListResponse;
import com.escuela.vehiculos.dto.VehiculoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VehiculoService {
    Page<VehiculoListResponse> findAll(Pageable pageable);
    VehiculoResponse findById(Long id);
    VehiculoResponse create(CreateVehiculoRequest request);
    VehiculoResponse update(Long id, UpdateVehiculoRequest request);
    void softDelete(Long id);
}
