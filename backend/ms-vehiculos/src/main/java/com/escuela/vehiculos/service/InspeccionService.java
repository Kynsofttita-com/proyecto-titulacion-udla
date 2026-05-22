package com.escuela.vehiculos.service;

import com.escuela.vehiculos.dto.InspeccionRequest;
import com.escuela.vehiculos.dto.InspeccionResponse;
import com.escuela.vehiculos.entity.Inspeccion;
import com.escuela.vehiculos.entity.Vehiculo;
import com.escuela.vehiculos.exception.RecursoNotFoundException;
import com.escuela.vehiculos.exception.VehiculoNotFoundException;
import com.escuela.vehiculos.repository.InspeccionRepository;
import com.escuela.vehiculos.repository.VehiculoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class InspeccionService {

    private static final Logger log = LoggerFactory.getLogger(InspeccionService.class);

    private final InspeccionRepository repository;
    private final VehiculoRepository vehiculoRepository;

    public InspeccionService(InspeccionRepository repository, VehiculoRepository vehiculoRepository) {
        this.repository = repository;
        this.vehiculoRepository = vehiculoRepository;
    }

    @Transactional(readOnly = true)
    public List<InspeccionResponse> listar(Long vehiculoId) {
        vehiculoOFallar(vehiculoId);
        return repository.findByVehiculoIdAndDeletedAtIsNullOrderByFechaDesc(vehiculoId)
                .stream().map(this::toResponse).toList();
    }

    public InspeccionResponse registrar(Long vehiculoId, InspeccionRequest request) {
        Vehiculo v = vehiculoOFallar(vehiculoId);
        Inspeccion i = Inspeccion.builder()
                .vehiculo(v)
                .tipo(request.tipo())
                .fecha(request.fecha())
                .resultado(request.resultado())
                .archivoUrl(request.archivoUrl())
                .observaciones(request.observaciones())
                .proximaInspeccion(request.proximaInspeccion())
                .build();
        i = repository.save(i);
        log.info("Inspeccion registrada id={} vehiculoId={} tipo={} resultado={}",
                i.getId(), vehiculoId, i.getTipo(), i.getResultado());
        return toResponse(i);
    }

    public InspeccionResponse actualizar(Long vehiculoId, Long inspeccionId, InspeccionRequest request) {
        Inspeccion i = repository.findByIdAndVehiculoIdAndDeletedAtIsNull(inspeccionId, vehiculoId)
                .orElseThrow(() -> new RecursoNotFoundException("Inspeccion", inspeccionId));
        i.setTipo(request.tipo());
        i.setFecha(request.fecha());
        i.setResultado(request.resultado());
        i.setArchivoUrl(request.archivoUrl());
        i.setObservaciones(request.observaciones());
        i.setProximaInspeccion(request.proximaInspeccion());
        repository.save(i);
        log.info("Inspeccion actualizada id={}", inspeccionId);
        return toResponse(i);
    }

    public void eliminar(Long vehiculoId, Long inspeccionId) {
        Inspeccion i = repository.findByIdAndVehiculoIdAndDeletedAtIsNull(inspeccionId, vehiculoId)
                .orElseThrow(() -> new RecursoNotFoundException("Inspeccion", inspeccionId));
        i.setDeletedAt(LocalDateTime.now());
        repository.save(i);
        log.info("Inspeccion soft-deleted id={}", inspeccionId);
    }

    private Vehiculo vehiculoOFallar(Long id) {
        return vehiculoRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new VehiculoNotFoundException(id));
    }

    private InspeccionResponse toResponse(Inspeccion i) {
        return new InspeccionResponse(i.getId(), i.getVehiculo().getId(),
                i.getTipo(), i.getFecha(), i.getResultado(),
                i.getArchivoUrl(), i.getObservaciones(), i.getProximaInspeccion());
    }
}
