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
        propagarFechaAlVehiculo(v, i);
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
        propagarFechaAlVehiculo(i.getVehiculo(), i);
        log.info("Inspeccion actualizada id={}", inspeccionId);
        return toResponse(i);
    }

    /**
     * Cuando se registra/actualiza una inspeccion SOAT o TECNICA con
     * resultado APROBADA o CONDICIONADA y con proximaInspeccion definida,
     * propagamos esa fecha al campo del vehiculo (soatVencimiento o
     * revisionVencimiento). Asi el "resumen del vehiculo" y las alertas
     * quedan sincronizadas con la inspeccion mas reciente.
     *
     * Reglas:
     *  - Solo propagamos si la nueva proximaInspeccion es POSTERIOR a la
     *    fecha actual del vehiculo (evita retroceder fecha por registro
     *    de una inspeccion vieja o correccion tardia). Si el usuario
     *    quiere forzar una fecha anterior, debe editar el vehiculo.
     *  - Resultado REPROBADA no propaga (el documento no fue renovado).
     *  - Tipo INTERNA no propaga (es solo control interno de la escuela).
     */
    private void propagarFechaAlVehiculo(Vehiculo v, Inspeccion i) {
        String resultado = i.getResultado();
        if (!"APROBADA".equals(resultado) && !"CONDICIONADA".equals(resultado)) return;
        java.time.LocalDate nueva = i.getProximaInspeccion();
        if (nueva == null) return;

        boolean actualizado = false;
        if ("SOAT".equals(i.getTipo())) {
            if (v.getSoatVencimiento() == null || nueva.isAfter(v.getSoatVencimiento())) {
                v.setSoatVencimiento(nueva);
                actualizado = true;
            }
        } else if ("TECNICA".equals(i.getTipo())) {
            if (v.getRevisionVencimiento() == null || nueva.isAfter(v.getRevisionVencimiento())) {
                v.setRevisionVencimiento(nueva);
                actualizado = true;
            }
        }
        if (actualizado) {
            vehiculoRepository.save(v);
            log.info("Vehiculo id={} sincronizado desde inspeccion {}: nueva fecha {}",
                    v.getId(), i.getTipo(), nueva);
        }
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
