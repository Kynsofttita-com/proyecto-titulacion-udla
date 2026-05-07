package com.escuela.asignaciones.repository;

import com.escuela.asignaciones.entity.CambioAsignacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la entidad {@link CambioAsignacion}.
 *
 * <p>Esqueleto base. Las queries específicas (findBy*, @Query custom, etc.) se
 * agregarán en sprints posteriores cuando se implementen los CRUDs.</p>
 */
@Repository
public interface CambioAsignacionRepository extends JpaRepository<CambioAsignacion, Long> {
}
