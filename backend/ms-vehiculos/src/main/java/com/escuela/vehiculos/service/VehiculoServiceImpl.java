package com.escuela.vehiculos.service;

import com.escuela.vehiculos.dto.ActualizarKilometrajeRequest;
import com.escuela.vehiculos.dto.ActualizarKilometrajeResponse;
import com.escuela.vehiculos.dto.CreateVehiculoRequest;
import com.escuela.vehiculos.dto.UpdateVehiculoRequest;
import com.escuela.vehiculos.dto.VehiculoListResponse;
import com.escuela.vehiculos.dto.VehiculoResponse;
import com.escuela.vehiculos.entity.Vehiculo;
import com.escuela.vehiculos.exception.PlacaDuplicadaException;
import com.escuela.vehiculos.exception.VehiculoNotFoundException;
import com.escuela.vehiculos.mapper.VehiculoMapper;
import com.escuela.vehiculos.mapper.VehiculoMapperImpl;
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
    private VehiculoMapper mapper;

    public VehiculoServiceImpl(VehiculoRepository repository) {
        this.repository = repository;
    }

    private VehiculoMapper getMapper() {
        if (mapper == null) {
            this.mapper = new VehiculoMapperImpl();
        }
        return mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VehiculoListResponse> findAll(Pageable pageable) {
        return repository.findByDeletedAtIsNull(pageable).map(getMapper()::toListResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VehiculoListResponse> buscar(Pageable pageable, String search, String estado) {
        return repository.buscar(search, estado, pageable).map(getMapper()::toListResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public VehiculoResponse findById(Long id) {
        Vehiculo vehiculo = repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new VehiculoNotFoundException(id));
        return getMapper().toResponse(vehiculo);
    }

    @Override
    public VehiculoResponse create(CreateVehiculoRequest request) {
        if (repository.existsByPlacaAndDeletedAtIsNull(request.placa())) {
            throw new PlacaDuplicadaException(request.placa());
        }

        Vehiculo vehiculo = getMapper().toEntity(request);
        if (vehiculo.getEstado() == null) {
            vehiculo.setEstado("ACTIVO");
        }
        vehiculo = repository.save(vehiculo);
        log.info("Vehículo creado id={} placa={}", vehiculo.getId(), vehiculo.getPlaca());
        return getMapper().toResponse(vehiculo);
    }

    @Override
    public VehiculoResponse update(Long id, UpdateVehiculoRequest request) {
        Vehiculo vehiculo = repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new VehiculoNotFoundException(id));

        getMapper().updateEntity(request, vehiculo);
        vehiculo.setUpdatedAt(LocalDateTime.now());
        vehiculo = repository.save(vehiculo);
        log.info("Vehículo actualizado id={}", id);
        return getMapper().toResponse(vehiculo);
    }

    @Override
    public ActualizarKilometrajeResponse actualizarKilometraje(Long id, ActualizarKilometrajeRequest request) {
        Vehiculo v = repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new VehiculoNotFoundException(id));
        Integer kmAnterior = v.getKilometraje() == null ? 0 : v.getKilometraje();
        Integer nuevoKm = request.nuevoKm();

        // Monotonico: solo aumenta. Si llega un km menor, se ignora silenciosamente.
        if (nuevoKm <= kmAnterior) {
            log.info("Kilometraje NO actualizado vehiculoId={} kmAnterior={} nuevoKm={} (no retroceso) fuente={}",
                    id, kmAnterior, nuevoKm, request.fuente());
            return new ActualizarKilometrajeResponse(id, kmAnterior, kmAnterior, false,
                    "Kilometraje propuesto (" + nuevoKm + ") es menor o igual al actual (" + kmAnterior + ")");
        }

        v.setKilometraje(nuevoKm);
        repository.save(v);
        log.info("Kilometraje actualizado vehiculoId={} {} -> {} fuente={}",
                id, kmAnterior, nuevoKm, request.fuente());
        return new ActualizarKilometrajeResponse(id, kmAnterior, nuevoKm, true,
                "Kilometraje actualizado de " + kmAnterior + " a " + nuevoKm);
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
