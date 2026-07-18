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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificacionService {

    private final NotificacionRepository repository;
    private final NotificacionMapper mapper;
    private final JdbcTemplate jdbcTemplate;

    /**
     * Crea una notificacion identica para cada usuario con rol ADMIN o STAFF.
     * Se usa para eventos que no tienen un dueno unico (ej: SOAT vencido de un
     * vehiculo de la flota).
     *
     * @return cantidad de notificaciones creadas (una por admin/staff activo).
     */
    public int crearNotificacionParaAdmins(String titulo, String mensaje, String tipo, String prioridad) {
        List<Long> ids = obtenerIdsAdminStaff();
        for (Long id : ids) {
            crearNotificacion(id, titulo, mensaje, tipo, prioridad);
        }
        return ids.size();
    }

    /** Consulta cross-schema a auth_schema para obtener usuarios ADMIN o STAFF activos. */
    @Transactional(readOnly = true)
    public List<Long> obtenerIdsAdminStaff() {
        return jdbcTemplate.queryForList(
                "SELECT u.id FROM auth_schema.usuarios u " +
                "JOIN auth_schema.usuario_rol ur ON u.id = ur.usuario_id " +
                "JOIN auth_schema.roles r ON ur.rol_id = r.id " +
                "WHERE u.activo = TRUE AND u.deleted_at IS NULL " +
                "  AND r.nombre IN ('ADMIN','STAFF')",
                Long.class);
    }

    public NotificacionResponse crearNotificacion(Long usuarioId, String titulo, String mensaje, String tipo, String prioridad) {
        Notificacion notificacion = new Notificacion();
        notificacion.setUsuarioId(usuarioId);
        notificacion.setTitulo(titulo);
        notificacion.setMensaje(mensaje);
        notificacion.setTipo(tipo);
        notificacion.setPrioridad(prioridad);
        notificacion.setLeida(false);
        notificacion.setFechaCreacion(LocalDateTime.now());

        Notificacion saved = repository.save(notificacion);
        log.info("Notificación creada: id={}, usuarioId={}, tipo={}", saved.getId(), usuarioId, tipo);
        return mapper.toResponse(saved);
    }

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

    public int marcarTodasComoLeidas(Long usuarioId) {
        int afectadas = repository.marcarTodasComoLeidas(usuarioId, LocalDateTime.now());
        log.info("Notificaciones marcadas como leidas: usuarioId={}, count={}", usuarioId, afectadas);
        return afectadas;
    }

    public int eliminarTodasPorUsuario(Long usuarioId) {
        int afectadas = repository.eliminarTodasPorUsuario(usuarioId, LocalDateTime.now());
        log.info("Notificaciones eliminadas (soft): usuarioId={}, count={}", usuarioId, afectadas);
        return afectadas;
    }
}
