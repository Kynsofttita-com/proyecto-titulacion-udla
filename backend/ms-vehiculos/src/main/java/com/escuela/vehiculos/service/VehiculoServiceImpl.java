package com.escuela.vehiculos.service;

import com.escuela.vehiculos.dto.CreateVehiculoRequest;
import com.escuela.vehiculos.dto.UpdateVehiculoRequest;
import com.escuela.vehiculos.dto.VehiculoListResponse;
import com.escuela.vehiculos.dto.VehiculoResponse;
import com.escuela.vehiculos.entity.Vehiculo;
import com.escuela.vehiculos.exception.PlacaDuplicadaException;
import com.escuela.vehiculos.exception.VehiculoNotFoundException;
import com.escuela.vehiculos.mapper.VehiculoMapper;
import com.escuela.vehiculos.repository.VehiculoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@Transactional
public class VehiculoServiceImpl implements VehiculoService {

    private final VehiculoRepository repository;
    private final VehiculoMapper mapper;

    public VehiculoServiceImpl(VehiculoRepository repository, VehiculoMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VehiculoListResponse> findAll(Pageable pageable) {
        return repository.findByDeletedAtIsNull(pageable).map(mapper::toListResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public VehiculoResponse findById(Long id) {
        Vehiculo vehiculo = repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new VehiculoNotFoundException(id));
        return mapper.toResponse(vehiculo);
    }

    @Override
    public VehiculoResponse create(CreateVehiculoRequest request) {
        if (repository.existsByPlacaAndDeletedAtIsNull(request.placa())) {
            throw new PlacaDuplicadaException(request.placa());
        }

        Vehiculo vehiculo = mapper.toEntity(request);
        vehiculo = repository.save(vehiculo);
        log.info("Vehículo creado id={} placa={}", vehiculo.getId(), vehiculo.getPlaca());
        return mapper.toResponse(vehiculo);
    }

    @Override
    public VehiculoResponse update(Long id, UpdateVehiculoRequest request) {
        Vehiculo vehiculo = repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new VehiculoNotFoundException(id));

        mapper.updateEntity(request, vehiculo);
        vehiculo.setUpdatedAt(LocalDateTime.now());
        vehiculo = repository.save(vehiculo);
        log.info("Vehículo actualizado id={}", id);
        return mapper.toResponse(vehiculo);
    }

    @Override
    public void softDelete(Long id) {
        Vehiculo vehiculo = repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new VehiculoNotFoundException(id));
        vehiculo.setDeletedAt(LocalDateTime.now());
        repository.save(vehiculo);
        log.info("Vehículo soft-deleted id={}", id);
    }
}
