package com.escuela.notificaciones.service;

import com.escuela.notificaciones.dto.PreferenciaResponse;
import com.escuela.notificaciones.dto.UpdatePreferenciaRequest;
import com.escuela.notificaciones.entity.PreferenciaNotificacion;
import com.escuela.notificaciones.exception.PreferenciaNotFoundException;
import com.escuela.notificaciones.mapper.PreferenciaNotificacionMapper;
import com.escuela.notificaciones.repository.PreferenciaNotificacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PreferenciaNotificacionService {

    private final PreferenciaNotificacionRepository repository;
    private final PreferenciaNotificacionMapper mapper;

    @Transactional(readOnly = true)
    public PreferenciaResponse obtenerPorUsuario(Long usuarioId) {
        PreferenciaNotificacion preferencia = repository.findByUsuarioId(usuarioId)
            .orElseThrow(() -> new PreferenciaNotFoundException(usuarioId));
        return mapper.toResponse(preferencia);
    }

    public PreferenciaResponse actualizar(Long usuarioId, UpdatePreferenciaRequest request) {
        PreferenciaNotificacion preferencia = repository.findByUsuarioId(usuarioId)
            .orElseThrow(() -> new PreferenciaNotFoundException(usuarioId));

        mapper.updateEntity(preferencia, request);
        PreferenciaNotificacion actualizada = repository.save(preferencia);
        log.info("Preferencia de notificación actualizada: usuarioId={}", usuarioId);
        return mapper.toResponse(actualizada);
    }

    public PreferenciaResponse crearOActualizar(Long usuarioId, UpdatePreferenciaRequest request) {
        PreferenciaNotificacion preferencia = repository.findByUsuarioId(usuarioId)
            .orElseGet(() -> PreferenciaNotificacion.builder()
                .usuarioId(usuarioId)
                .build()
            );

        mapper.updateEntity(preferencia, request);
        PreferenciaNotificacion guardada = repository.save(preferencia);
        log.info("Preferencia de notificación creada/actualizada: usuarioId={}", usuarioId);
        return mapper.toResponse(guardada);
    }
}
