package com.escuela.cobros.service;

import com.escuela.cobros.dto.CategoriaMovimientoRequest;
import com.escuela.cobros.dto.CategoriaMovimientoResponse;
import com.escuela.cobros.entity.CategoriaMovimiento;
import com.escuela.cobros.repository.CategoriaMovimientoRepository;
import com.escuela.cobros.repository.MovimientoContableRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class CategoriaMovimientoService {

    private static final Logger log = LoggerFactory.getLogger(CategoriaMovimientoService.class);

    private final CategoriaMovimientoRepository repository;
    private final MovimientoContableRepository movimientosRepo;

    public CategoriaMovimientoService(CategoriaMovimientoRepository repository,
                                     MovimientoContableRepository movimientosRepo) {
        this.repository = repository;
        this.movimientosRepo = movimientosRepo;
    }

    @Transactional(readOnly = true)
    public List<CategoriaMovimientoResponse> listarTodas() {
        return repository.findAllByOrderByTipoAscNombreAsc().stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<CategoriaMovimientoResponse> listarActivas(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            return repository.findByActivoTrueOrderByTipoAscNombreAsc().stream()
                    .map(this::toResponse).toList();
        }
        return repository.findByTipoAndActivoTrueOrderByNombreAsc(tipo).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CategoriaMovimientoResponse obtener(Long id) {
        return toResponse(findOrThrow(id));
    }

    public CategoriaMovimientoResponse crear(CategoriaMovimientoRequest request) {
        String codigo = request.codigo().trim().toUpperCase();
        repository.findByCodigo(codigo).ifPresent(x -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe una categoria con codigo " + codigo);
        });
        CategoriaMovimiento c = CategoriaMovimiento.builder()
                .codigo(codigo)
                .nombre(request.nombre().trim())
                .tipo(request.tipo())
                .esSistema(false)
                .activo(request.activo() != null ? request.activo() : true)
                .build();
        c = repository.save(c);
        log.info("Categoria creada: codigo={} nombre={} tipo={}", c.getCodigo(), c.getNombre(), c.getTipo());
        return toResponse(c);
    }

    public CategoriaMovimientoResponse actualizar(Long id, CategoriaMovimientoRequest request) {
        CategoriaMovimiento c = findOrThrow(id);
        // Las de sistema: solo nombre editable
        if (Boolean.TRUE.equals(c.getEsSistema())) {
            c.setNombre(request.nombre().trim());
            log.info("Categoria de sistema id={} nombre actualizado a '{}'", id, c.getNombre());
        } else {
            String codigo = request.codigo().trim().toUpperCase();
            if (!c.getCodigo().equals(codigo)) {
                repository.findByCodigo(codigo).ifPresent(otra -> {
                    if (!otra.getId().equals(id)) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT,
                                "Ya existe otra categoria con codigo " + codigo);
                    }
                });
                c.setCodigo(codigo);
            }
            c.setNombre(request.nombre().trim());
            c.setTipo(request.tipo());
            if (request.activo() != null) c.setActivo(request.activo());
        }
        c = repository.save(c);
        return toResponse(c);
    }

    public void desactivar(Long id) {
        CategoriaMovimiento c = findOrThrow(id);
        if (Boolean.TRUE.equals(c.getEsSistema())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No se puede desactivar una categoria del sistema. Solo se puede editar el nombre.");
        }
        if (!movimientosRepo.findByCategoriaIdAndAnuladoFalse(id).isEmpty()) {
            // Tiene movimientos activos: solo desactivar (no borrar fisico)
            c.setActivo(false);
            repository.save(c);
            log.info("Categoria id={} desactivada (tiene movimientos activos)", id);
        } else {
            repository.delete(c);
            log.info("Categoria id={} eliminada (sin movimientos)", id);
        }
    }

    private CategoriaMovimiento findOrThrow(Long id) {
        return repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Categoria no encontrada: " + id));
    }

    private CategoriaMovimientoResponse toResponse(CategoriaMovimiento c) {
        return new CategoriaMovimientoResponse(
                c.getId(),
                c.getCodigo(),
                c.getNombre(),
                c.getTipo(),
                c.getEsSistema(),
                c.getActivo(),
                c.getCreatedAt(),
                c.getUpdatedAt()
        );
    }
}
