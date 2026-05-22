package com.escuela.auth.service;

import com.escuela.auth.dto.ConfiguracionResponse;
import com.escuela.auth.dto.UpdateConfiguracionRequest;
import com.escuela.auth.entity.ConfiguracionEscuela;
import com.escuela.auth.exception.DuplicateResourceException;
import com.escuela.auth.exception.ResourceNotFoundException;
import com.escuela.auth.mapper.ConfiguracionMapper;
import com.escuela.auth.repository.ConfiguracionEscuelaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de configuracion de la escuela (single-tenant).
 *
 * <p>Solo existe una fila en {@code configuracion_escuela}; el GET la devuelve
 * y el PUT la actualiza. No hay POST ni DELETE.</p>
 */
@Service
@Transactional
public class ConfiguracionService {

    private static final Logger log = LoggerFactory.getLogger(ConfiguracionService.class);

    private final ConfiguracionEscuelaRepository repository;
    private final ConfiguracionMapper mapper;

    public ConfiguracionService(ConfiguracionEscuelaRepository repository,
                                ConfiguracionMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public ConfiguracionResponse obtener() {
        ConfiguracionEscuela conf = repository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Configuracion", "default"));
        return mapper.toResponse(conf);
    }

    public ConfiguracionResponse actualizar(UpdateConfiguracionRequest request) {
        ConfiguracionEscuela conf = repository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Configuracion", "default"));

        // RUC unico: si cambia, validar que no choque con otra fila (defensivo aunque sea single-tenant)
        if (!conf.getRuc().equals(request.ruc())
                && repository.findByRuc(request.ruc()).isPresent()) {
            throw new DuplicateResourceException("Ya existe otra configuracion con RUC " + request.ruc());
        }

        mapper.updateEntity(request, conf);
        repository.save(conf);
        log.info("Configuracion actualizada id={}", conf.getId());
        return mapper.toResponse(conf);
    }
}
