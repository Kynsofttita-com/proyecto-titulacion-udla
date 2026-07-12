package com.escuela.notificaciones.service;

import com.escuela.notificaciones.dto.CreatePlantillaRequest;
import com.escuela.notificaciones.dto.PlantillaResponse;
import com.escuela.notificaciones.dto.UpdatePlantillaRequest;
import com.escuela.notificaciones.entity.Plantilla;
import com.escuela.notificaciones.exception.PlantillaNotFoundException;
import com.escuela.notificaciones.mapper.PlantillaMapper;
import com.escuela.notificaciones.repository.PlantillaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PlantillaService {

    private final PlantillaRepository repository;
    private final PlantillaMapper mapper;

    public PlantillaResponse crear(CreatePlantillaRequest request) {
        if (repository.existsByCodigoAndDeletedAtIsNull(request.codigo())) {
            throw new IllegalArgumentException("Plantilla con código '" + request.codigo() + "' ya existe");
        }
        Plantilla plantilla = mapper.toEntity(request);
        Plantilla guardada = repository.save(plantilla);
        log.info("Plantilla creada: id={}, codigo={}", guardada.getId(), guardada.getCodigo());
        return mapper.toResponse(guardada);
    }

    @Transactional(readOnly = true)
    public PlantillaResponse obtenerPorId(Long id) {
        Plantilla plantilla = repository.findById(id)
            .orElseThrow(() -> new PlantillaNotFoundException(id));
        return mapper.toResponse(plantilla);
    }

    @Transactional(readOnly = true)
    public PlantillaResponse obtenerPorCodigo(String codigo) {
        Plantilla plantilla = repository.findByCodigoAndDeletedAtIsNull(codigo)
            .orElseThrow(() -> new PlantillaNotFoundException(codigo));
        return mapper.toResponse(plantilla);
    }

    @Transactional(readOnly = true)
    public Page<PlantillaResponse> listarActivas(Pageable pageable) {
        return repository.findByActivaTrueAndDeletedAtIsNull(pageable)
            .map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<PlantillaResponse> listarTodas(Pageable pageable) {
        return repository.findByDeletedAtIsNull(pageable)
            .map(mapper::toResponse);
    }

    public PlantillaResponse actualizar(Long id, UpdatePlantillaRequest request) {
        Plantilla plantilla = repository.findById(id)
            .orElseThrow(() -> new PlantillaNotFoundException(id));
        mapper.updateEntity(plantilla, request);
        Plantilla actualizada = repository.save(plantilla);
        log.info("Plantilla actualizada: id={}, codigo={}", actualizada.getId(), actualizada.getCodigo());
        return mapper.toResponse(actualizada);
    }

    public void eliminar(Long id) {
        Plantilla plantilla = repository.findById(id)
            .orElseThrow(() -> new PlantillaNotFoundException(id));
        plantilla.setDeletedAt(java.time.LocalDateTime.now());
        repository.save(plantilla);
        log.info("Plantilla eliminada (soft): id={}", id);
    }

}
