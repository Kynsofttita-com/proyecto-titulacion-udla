package com.escuela.vehiculos.service;

import com.escuela.vehiculos.dto.ActualizarPrecioRequest;
import com.escuela.vehiculos.dto.CrearTipoCombustibleRequest;
import com.escuela.vehiculos.dto.TipoCombustibleResponse;
import com.escuela.vehiculos.entity.TipoCombustible;
import com.escuela.vehiculos.repository.TipoCombustibleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@Service
@Transactional
public class TipoCombustibleService {

    private static final Logger log = LoggerFactory.getLogger(TipoCombustibleService.class);

    private final TipoCombustibleRepository repository;

    public TipoCombustibleService(TipoCombustibleRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<TipoCombustibleResponse> listarTodos() {
        return repository.findAllByOrderByCodigoAsc()
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<TipoCombustibleResponse> listarActivos() {
        return repository.findByActivoTrueOrderByCodigoAsc()
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public TipoCombustibleResponse obtener(Long id) {
        TipoCombustible t = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "TipoCombustible no encontrado: " + id));
        return toResponse(t);
    }

    public TipoCombustibleResponse crear(CrearTipoCombustibleRequest request) {
        // Validar que el codigo no exista (incluyendo desactivados — el codigo es global)
        repository.findByCodigo(request.codigo()).ifPresent(t -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un tipo de combustible con codigo " + request.codigo());
        });
        TipoCombustible t = TipoCombustible.builder()
                .codigo(request.codigo())
                .nombre(request.nombre())
                .unidad(request.unidad())
                .precioActual(request.precioActual())
                .activo(true)
                .observaciones(request.observaciones())
                .build();
        t = repository.save(t);
        log.info("TipoCombustible creado: codigo={} precio={}/{}", t.getCodigo(), t.getPrecioActual(), t.getUnidad());
        return toResponse(t);
    }

    public TipoCombustibleResponse actualizarPrecio(Long id, ActualizarPrecioRequest request) {
        TipoCombustible t = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "TipoCombustible no encontrado: " + id));
        t.setPrecioActual(request.precioActual());
        if (request.activo() != null) t.setActivo(request.activo());
        if (request.observaciones() != null) t.setObservaciones(request.observaciones());
        repository.save(t);
        log.info("Precio actualizado: tipo={} nuevo_precio={}", t.getCodigo(), t.getPrecioActual());
        return toResponse(t);
    }

    /**
     * Desactiva un tipo (no se borra fisico porque los vehiculos pueden tener FK a el).
     * Si esta en uso por algun vehiculo, lanza CONFLICT.
     */
    public void desactivar(Long id) {
        TipoCombustible t = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "TipoCombustible no encontrado: " + id));
        t.setActivo(false);
        repository.save(t);
        log.info("TipoCombustible desactivado: codigo={}", t.getCodigo());
    }

    private TipoCombustibleResponse toResponse(TipoCombustible t) {
        return new TipoCombustibleResponse(
                t.getId(),
                t.getCodigo(),
                t.getNombre(),
                t.getUnidad(),
                t.getPrecioActual(),
                t.getActivo(),
                t.getObservaciones(),
                t.getUpdatedAt(),
                t.getUpdatedBy()
        );
    }
}
