package com.escuela.vehiculos.service;

import com.escuela.vehiculos.dto.DocumentoVehiculoRequest;
import com.escuela.vehiculos.dto.DocumentoVehiculoResponse;
import com.escuela.vehiculos.entity.DocumentoVehiculo;
import com.escuela.vehiculos.entity.Vehiculo;
import com.escuela.vehiculos.exception.RecursoNotFoundException;
import com.escuela.vehiculos.exception.VehiculoNotFoundException;
import com.escuela.vehiculos.repository.DocumentoVehiculoRepository;
import com.escuela.vehiculos.repository.VehiculoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class DocumentoVehiculoService {

    private static final Logger log = LoggerFactory.getLogger(DocumentoVehiculoService.class);

    private final DocumentoVehiculoRepository repository;
    private final VehiculoRepository vehiculoRepository;

    public DocumentoVehiculoService(DocumentoVehiculoRepository repository,
                                    VehiculoRepository vehiculoRepository) {
        this.repository = repository;
        this.vehiculoRepository = vehiculoRepository;
    }

    @Transactional(readOnly = true)
    public List<DocumentoVehiculoResponse> listar(Long vehiculoId) {
        vehiculoOFallar(vehiculoId);
        return repository.findByVehiculoIdAndDeletedAtIsNullOrderByFechaVencimientoAsc(vehiculoId)
                .stream().map(this::toResponse).toList();
    }

    public DocumentoVehiculoResponse subir(Long vehiculoId, DocumentoVehiculoRequest request) {
        Vehiculo v = vehiculoOFallar(vehiculoId);
        DocumentoVehiculo d = DocumentoVehiculo.builder()
                .vehiculo(v)
                .tipo(request.tipo())
                .numero(request.numero())
                .urlArchivo(request.urlArchivo())
                .fechaEmision(request.fechaEmision())
                .fechaVencimiento(request.fechaVencimiento())
                .build();
        d = repository.save(d);
        log.info("DocumentoVehiculo subido id={} vehiculoId={} tipo={}",
                d.getId(), vehiculoId, d.getTipo());
        return toResponse(d);
    }

    public DocumentoVehiculoResponse actualizar(Long vehiculoId, Long docId,
                                                DocumentoVehiculoRequest request) {
        DocumentoVehiculo d = repository.findByIdAndVehiculoIdAndDeletedAtIsNull(docId, vehiculoId)
                .orElseThrow(() -> new RecursoNotFoundException("DocumentoVehiculo", docId));
        d.setTipo(request.tipo());
        d.setNumero(request.numero());
        d.setUrlArchivo(request.urlArchivo());
        d.setFechaEmision(request.fechaEmision());
        d.setFechaVencimiento(request.fechaVencimiento());
        repository.save(d);
        log.info("DocumentoVehiculo actualizado id={}", docId);
        return toResponse(d);
    }

    public void eliminar(Long vehiculoId, Long docId) {
        DocumentoVehiculo d = repository.findByIdAndVehiculoIdAndDeletedAtIsNull(docId, vehiculoId)
                .orElseThrow(() -> new RecursoNotFoundException("DocumentoVehiculo", docId));
        d.setDeletedAt(LocalDateTime.now());
        repository.save(d);
        log.info("DocumentoVehiculo soft-deleted id={}", docId);
    }

    private Vehiculo vehiculoOFallar(Long id) {
        return vehiculoRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new VehiculoNotFoundException(id));
    }

    private DocumentoVehiculoResponse toResponse(DocumentoVehiculo d) {
        return new DocumentoVehiculoResponse(d.getId(), d.getVehiculo().getId(),
                d.getTipo(), d.getNumero(), d.getUrlArchivo(),
                d.getFechaEmision(), d.getFechaVencimiento());
    }
}
