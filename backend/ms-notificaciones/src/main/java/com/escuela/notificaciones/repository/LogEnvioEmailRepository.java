package com.escuela.notificaciones.repository;

import com.escuela.notificaciones.entity.LogEnvioEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la entidad {@link LogEnvioEmail}.
 *
 * <p>Esqueleto base. Las queries específicas (findBy*, @Query custom, etc.) se
 * agregarán en sprints posteriores cuando se implementen los CRUDs.</p>
 */
@Repository
public interface LogEnvioEmailRepository extends JpaRepository<LogEnvioEmail, Long> {
}
