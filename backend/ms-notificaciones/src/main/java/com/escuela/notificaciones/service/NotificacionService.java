package com.escuela.notificaciones.service;

import com.escuela.notificaciones.dto.NotificacionResponse;
import com.escuela.notificaciones.entity.Notificacion;
import com.escuela.notificaciones.exception.NotificacionNotFoundException;
import com.escuela.notificaciones.mapper.NotificacionMapper;
import com.escuela.notificaciones.repository.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificacionService {

    private final NotificacionRepository repository;
    private final NotificacionMapper mapper;

    @Transactional(readOnly = true)
    public NotificacionResponse obtenerPorId(Long id) {
        Notificacion notificacion = repository.findById(id)
            .orElseThrow(() -> new NotificacionNotFoundException(id));
        return mapper.toResponse(notificacion);
    }

    @Transactional(readOnly = true)
    public Page<NotificacionResponse> listarPorUsuario(Long usuarioId, Pageable pageable) {
        return repository.findByUsuarioId(usuarioId, pageable)
            .map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<NotificacionResponse> listarPorUsuarioConFiltros(
        Long usuarioId,
        Boolean leida,
        String tipo,
        String prioridad,
        Pageable pageable
    ) {
        if (leida != null && tipo != null && prioridad != null) {
            return repository.findByUsuarioIdWithFilters(usuarioId, leida, tipo, prioridad, pageable)
                .map(mapper::toResponse);
        } else if (leida != null) {
            return repository.findByUsuarioIdAndLeida(usuarioId, leida, pageable)
                .map(mapper::toResponse);
        } else if (tipo != null) {
            return repository.findByUsuarioIdAndTipo(usuarioId, tipo, pageable)
                .map(mapper::toResponse);
        } else if (prioridad != null) {
            return repository.findByUsuarioIdAndPrioridad(usuarioId, prioridad, pageable)
                .map(mapper::toResponse);
        } else {
            return listarPorUsuario(usuarioId, pageable);
        }
    }

    public NotificacionResponse marcarComoLeida(Long id) {
        Notificacion notificacion = repository.findById(id)
            .orElseThrow(() -> new NotificacionNotFoundException(id));

        if (!notificacion.getLeida()) {
            notificacion.setLeida(Boolean.TRUE);
            notificacion.setFechaLectura(LocalDateTime.now());
            repository.save(notificacion);
            log.info("Notificación marcada como leída: id={}", id);
        }

        return mapper.toResponse(notificacion);
    }

    public void eliminar(Long id) {
        Notificacion notificacion = repository.findById(id)
            .orElseThrow(() -> new NotificacionNotFoundException(id));
        notificacion.setDeletedAt(LocalDateTime.now());
        repository.save(notificacion);
        log.info("Notificación eliminada (soft): id={}", id);
    }
}
