package com.escuela.vehiculos.service;

import com.escuela.vehiculos.dto.CombustibleRequest;
import com.escuela.vehiculos.dto.CombustibleResponse;
import com.escuela.vehiculos.entity.RegistroCombustible;
import com.escuela.vehiculos.entity.Vehiculo;
import com.escuela.vehiculos.exception.RecursoNotFoundException;
import com.escuela.vehiculos.exception.VehiculoNotFoundException;
import com.escuela.vehiculos.repository.RegistroCombustibleRepository;
import com.escuela.vehiculos.repository.VehiculoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CombustibleService {

    private static final Logger log = LoggerFactory.getLogger(CombustibleService.class);

    private final RegistroCombustibleRepository repository;
    private final VehiculoRepository vehiculoRepository;

    public CombustibleService(RegistroCombustibleRepository repository, VehiculoRepository vehiculoRepository) {
        this.repository = repository;
        this.vehiculoRepository = vehiculoRepository;
    }

    @Transactional(readOnly = true)
    public Page<CombustibleResponse> listar(Long vehiculoId, Pageable pageable) {
        vehiculoOFallar(vehiculoId);
        return repository.findByVehiculoIdOrderByFechaDesc(vehiculoId, pageable).map(this::toResponse);
    }

    public CombustibleResponse registrar(Long vehiculoId, CombustibleRequest request) {
        Vehiculo v = vehiculoOFallar(vehiculoId);
        if (request.kilometrajeActual() < (v.getKilometraje() == null ? 0 : v.getKilometraje())) {
            throw new IllegalArgumentException(
                    "Kilometraje actual (" + request.kilometrajeActual() +
                            ") no puede ser menor al actual del vehiculo (" + v.getKilometraje() + ")");
        }
        RegistroCombustible r = RegistroCombustible.builder()
                .vehiculo(v)
                .fecha(request.fecha())
                .litros(request.litros())
                .costoTotal(request.costoTotal())
                .kilometrajeActual(request.kilometrajeActual())
                .estacion(request.estacion())
                .build();
        r = repository.save(r);

        // Actualiza el kilometraje del vehiculo (defensivo)
        v.setKilometraje(request.kilometrajeActual());
        vehiculoRepository.save(v);

        log.info("Combustible registrado id={} vehiculoId={} litros={}",
                r.getId(), vehiculoId, r.getLitros());
        return toResponse(r);
    }

    public void eliminar(Long vehiculoId, Long registroId) {
        RegistroCombustible r = repository.findByIdAndVehiculoId(registroId, vehiculoId)
                .orElseThrow(() -> new RecursoNotFoundException("RegistroCombustible", registroId));
        // Hard delete (no extiende BaseEntity)
        repository.delete(r);
        log.info("Combustible eliminado id={}", registroId);
    }

    private Vehiculo vehiculoOFallar(Long id) {
        return vehiculoRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new VehiculoNotFoundException(id));
    }

    private CombustibleResponse toResponse(RegistroCombustible r) {
        return new CombustibleResponse(r.getId(), r.getVehiculo().getId(),
                r.getFecha(), r.getLitros(), r.getCostoTotal(),
                r.getCostoPorLitro(), r.getKilometrajeActual(), r.getEstacion());
    }
}
