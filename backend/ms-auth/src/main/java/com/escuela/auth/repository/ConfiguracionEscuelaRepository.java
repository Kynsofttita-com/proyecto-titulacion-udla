package com.escuela.auth.repository;

import com.escuela.auth.entity.ConfiguracionEscuela;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la entidad {@link ConfiguracionEscuela}.
 *
 * <p>Esqueleto base. Las queries específicas (findBy*, @Query custom, etc.) se
 * agregarán en sprints posteriores cuando se implementen los CRUDs.</p>
 */
@Repository
public interface ConfiguracionEscuelaRepository extends JpaRepository<ConfiguracionEscuela, Long> {
}
