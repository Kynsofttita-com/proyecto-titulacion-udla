package com.escuela.vehiculos.service;

import com.escuela.vehiculos.dto.CreateVehiculoRequest;
import com.escuela.vehiculos.dto.UpdateVehiculoRequest;
import com.escuela.vehiculos.dto.VehiculoListResponse;
import com.escuela.vehiculos.dto.VehiculoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VehiculoService {
    Page<VehiculoListResponse> findAll(Pageable pageable);
    Page<VehiculoListResponse> buscar(Pageable pageable, String search, String estado);
    VehiculoResponse findById(Long id);
    VehiculoResponse create(CreateVehiculoRequest request);
    VehiculoResponse update(Long id, UpdateVehiculoRequest request);
    void softDelete(Long id);

    com.escuela.vehiculos.dto.ActualizarKilometrajeResponse actualizarKilometraje(
            Long id, com.escuela.vehiculos.dto.ActualizarKilometrajeRequest request);
}
