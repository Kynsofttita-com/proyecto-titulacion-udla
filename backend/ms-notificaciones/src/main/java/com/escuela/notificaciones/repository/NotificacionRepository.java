package com.escuela.notificaciones.repository;

import com.escuela.notificaciones.entity.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la entidad {@link Notificacion}.
 *
 * <p>Esqueleto base. Las queries específicas (findBy*, @Query custom, etc.) se
 * agregarán en sprints posteriores cuando se implementen los CRUDs.</p>
 */
@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
}
