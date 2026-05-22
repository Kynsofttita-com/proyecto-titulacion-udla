package com.escuela.vehiculos.service;

import com.escuela.vehiculos.dto.MantenimientoRequest;
import com.escuela.vehiculos.dto.MantenimientoResponse;
import com.escuela.vehiculos.entity.Mantenimiento;
import com.escuela.vehiculos.entity.Vehiculo;
import com.escuela.vehiculos.exception.RecursoNotFoundException;
import com.escuela.vehiculos.exception.VehiculoNotFoundException;
import com.escuela.vehiculos.repository.MantenimientoRepository;
import com.escuela.vehiculos.repository.VehiculoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class MantenimientoService {

    private static final Logger log = LoggerFactory.getLogger(MantenimientoService.class);

    private final MantenimientoRepository repository;
    private final VehiculoRepository vehiculoRepository;

    public MantenimientoService(MantenimientoRepository repository, VehiculoRepository vehiculoRepository) {
        this.repository = repository;
        this.vehiculoRepository = vehiculoRepository;
    }

    @Transactional(readOnly = true)
    public List<MantenimientoResponse> listar(Long vehiculoId) {
        vehiculoOFallar(vehiculoId);
        return repository.findByVehiculoIdAndDeletedAtIsNullOrderByFechaDesc(vehiculoId)
                .stream().map(this::toResponse).toList();
    }

    public MantenimientoResponse registrar(Long vehiculoId, MantenimientoRequest request) {
        Vehiculo v = vehiculoOFallar(vehiculoId);
        Mantenimiento m = Mantenimiento.builder()
                .vehiculo(v)
                .tipo(request.tipo().toUpperCase())
                .fecha(request.fecha())
                .costo(request.costo())
                .descripcion(request.descripcion())
                .taller(request.taller())
                .kilometrajeServicio(request.kilometrajeServicio())
                .proximaFecha(request.proximaFecha())
                .build();
        m = repository.save(m);
        log.info("Mantenimiento registrado id={} vehiculoId={} tipo={}",
                m.getId(), vehiculoId, m.getTipo());
        return toResponse(m);
    }

    public MantenimientoResponse actualizar(Long vehiculoId, Long mantId, MantenimientoRequest request) {
        Mantenimiento m = repository.findByIdAndVehiculoIdAndDeletedAtIsNull(mantId, vehiculoId)
                .orElseThrow(() -> new RecursoNotFoundException("Mantenimiento", mantId));
        m.setTipo(request.tipo().toUpperCase());
        m.setFecha(request.fecha());
        m.setCosto(request.costo());
        m.setDescripcion(request.descripcion());
        m.setTaller(request.taller());
        m.setKilometrajeServicio(request.kilometrajeServicio());
        m.setProximaFecha(request.proximaFecha());
        repository.save(m);
        log.info("Mantenimiento actualizado id={}", mantId);
        return toResponse(m);
    }

    public void eliminar(Long vehiculoId, Long mantId) {
        Mantenimiento m = repository.findByIdAndVehiculoIdAndDeletedAtIsNull(mantId, vehiculoId)
                .orElseThrow(() -> new RecursoNotFoundException("Mantenimiento", mantId));
        m.setDeletedAt(LocalDateTime.now());
        repository.save(m);
        log.info("Mantenimiento soft-deleted id={}", mantId);
    }

    private Vehiculo vehiculoOFallar(Long id) {
        return vehiculoRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new VehiculoNotFoundException(id));
    }

    private MantenimientoResponse toResponse(Mantenimiento m) {
        return new MantenimientoResponse(m.getId(), m.getVehiculo().getId(),
                m.getTipo(), m.getFecha(), m.getCosto(), m.getDescripcion(),
                m.getTaller(), m.getKilometrajeServicio(), m.getProximaFecha());
    }
}
