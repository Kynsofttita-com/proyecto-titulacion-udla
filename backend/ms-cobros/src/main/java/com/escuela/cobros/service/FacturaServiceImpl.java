package com.escuela.cobros.service;

import com.escuela.cobros.client.EstudianteClient;
import com.escuela.cobros.dto.EstudianteDetailDTO;
import com.escuela.cobros.dto.FacturaListResponse;
import com.escuela.cobros.dto.FacturaRequest;
import com.escuela.cobros.dto.FacturaResponse;
import com.escuela.cobros.entity.Factura;
import com.escuela.cobros.exception.EstudianteInactivoException;
import com.escuela.cobros.exception.EstudianteNotFoundException;
import com.escuela.cobros.exception.FacturaNotFoundException;
import com.escuela.cobros.mapper.FacturaMapper;
import com.escuela.cobros.repository.FacturaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class FacturaServiceImpl implements FacturaService {

    private final FacturaRepository facturaRepository;
    private final FacturaMapper facturaMapper;
    private final EstudianteClient estudianteClient;
    private final FacturaEventDispatcher eventDispatcher;

    @Override
    @Transactional(readOnly = true)
    public Page<FacturaListResponse> findAll(Pageable pageable) {
        log.debug("Buscando todas las facturas con paginación: {}", pageable);
        return facturaRepository.findByDeletedAtIsNull(pageable)
            .map(facturaMapper::toListResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public FacturaResponse findById(Long id) {
        log.debug("Buscando factura con ID: {}", id);
        Factura factura = facturaRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new FacturaNotFoundException(id));
        return facturaMapper.toResponse(factura);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FacturaListResponse> findByEstudianteId(Long estudianteId, Pageable pageable) {
        log.debug("Buscando facturas del estudiante: {} con paginación: {}", estudianteId, pageable);
        return facturaRepository.findByEstudianteIdAndDeletedAtIsNull(estudianteId, pageable)
            .map(facturaMapper::toListResponse);
    }

    @Override
    public FacturaResponse create(FacturaRequest request) {
        log.info("Creando factura para estudiante: {}", request.estudianteId());

        validarEstudianteExisteYActivo(request.estudianteId());

        String numeroFactura = generarNumeroFactura();

        Factura factura = facturaMapper.toEntity(request);
        factura.setNumeroFactura(numeroFactura);
        factura.setEstado("PENDIENTE");

        Factura guardada = facturaRepository.save(factura);
        log.info("Factura creada con ID: {} y número: {}", guardada.getId(), numeroFactura);

        return facturaMapper.toResponse(guardada);
    }

    @Override
    public FacturaResponse update(Long id, FacturaRequest request) {
        log.info("Actualizando factura con ID: {}", id);

        Factura factura = facturaRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new FacturaNotFoundException(id));

        validarEstudianteExisteYActivo(request.estudianteId());

        facturaMapper.updateEntity(request, factura);
        Factura actualizada = facturaRepository.save(factura);

        log.info("Factura actualizada con ID: {}", id);
        return facturaMapper.toResponse(actualizada);
    }

    @Override
    public void softDelete(Long id) {
        log.info("Soft-deletando factura con ID: {}", id);

        Factura factura = facturaRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new FacturaNotFoundException(id));

        factura.setDeletedAt(LocalDateTime.now());
        facturaRepository.save(factura);

        eventDispatcher.publishCancelada(factura);
        log.info("Factura soft-deletada con ID: {}", id);
    }

    private void validarEstudianteExisteYActivo(Long estudianteId) {
        try {
            EstudianteDetailDTO estudiante = estudianteClient.obtenerEstudiante(estudianteId);

            if ("INACTIVO".equalsIgnoreCase(estudiante.estado())) {
                log.warn("Estudiante {} se encuentra inactivo", estudianteId);
                throw new EstudianteInactivoException(estudianteId);
            }
        } catch (Exception e) {
            if (e instanceof EstudianteInactivoException) {
                throw e;
            }
            log.warn("No se pudo obtener estudiante {} via Feign", estudianteId, e);
            throw new EstudianteNotFoundException(estudianteId);
        }
    }

    private String generarNumeroFactura() {
        Long maxNumber = facturaRepository.findMaxNumeroFactura();
        long newNumber = (maxNumber != null ? maxNumber : 0) + 1;
        return String.format("FAC-%04d", newNumber);
    }
}
