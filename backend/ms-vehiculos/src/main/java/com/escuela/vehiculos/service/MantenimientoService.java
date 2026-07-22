package com.escuela.vehiculos.service;

import com.escuela.vehiculos.client.CobrosClient;
import com.escuela.vehiculos.dto.MantenimientoRequest;
import com.escuela.vehiculos.dto.MantenimientoResponse;
import com.escuela.vehiculos.dto.MovimientoVehiculoRequest;
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
    private final CobrosClient cobrosClient;

    public MantenimientoService(MantenimientoRepository repository,
                                VehiculoRepository vehiculoRepository,
                                CobrosClient cobrosClient) {
        this.repository = repository;
        this.vehiculoRepository = vehiculoRepository;
        this.cobrosClient = cobrosClient;
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

        sincronizarConCobrosCrear(m, v);
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

        sincronizarConCobrosActualizar(m);
        return toResponse(m);
    }

    public void eliminar(Long vehiculoId, Long mantId) {
        Mantenimiento m = repository.findByIdAndVehiculoIdAndDeletedAtIsNull(mantId, vehiculoId)
                .orElseThrow(() -> new RecursoNotFoundException("Mantenimiento", mantId));
        m.setDeletedAt(LocalDateTime.now());
        repository.save(m);
        log.info("Mantenimiento soft-deleted id={}", mantId);

        try {
            cobrosClient.anularMovimientoMantenimiento(mantId,
                    "Mantenimiento #" + mantId + " eliminado en Vehiculos");
        } catch (Exception ex) {
            log.warn("No se pudo anular el movimiento contable del mantenimiento {}: {}",
                    mantId, ex.getMessage());
        }
    }

    private void sincronizarConCobrosCrear(Mantenimiento m, Vehiculo v) {
        try {
            cobrosClient.crearMovimientoMantenimiento(m.getId(), toMovRequest(m, v));
        } catch (Exception ex) {
            log.warn("No se pudo sincronizar el gasto contable del mantenimiento {}: {}",
                    m.getId(), ex.getMessage());
        }
    }

    private void sincronizarConCobrosActualizar(Mantenimiento m) {
        try {
            cobrosClient.actualizarMovimientoMantenimiento(m.getId(), toMovRequest(m, m.getVehiculo()));
        } catch (Exception ex) {
            log.warn("No se pudo actualizar el gasto contable del mantenimiento {}: {}",
                    m.getId(), ex.getMessage());
        }
    }

    private MovimientoVehiculoRequest toMovRequest(Mantenimiento m, Vehiculo v) {
        String desc = m.getTipo() + ": " + m.getDescripcion();
        if (m.getTaller() != null && !m.getTaller().isBlank()) {
            desc += " (" + m.getTaller() + ")";
        }
        return new MovimientoVehiculoRequest(
                m.getFecha(),
                m.getCosto(),
                v.getId(),
                v.getPlaca(),
                m.getKilometrajeServicio(),
                desc,
                v.getPlaca()
        );
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
