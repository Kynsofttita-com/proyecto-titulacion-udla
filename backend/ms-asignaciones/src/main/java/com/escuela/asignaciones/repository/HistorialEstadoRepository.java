package com.escuela.asignaciones.repository;

import com.escuela.asignaciones.entity.HistorialEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la entidad {@link HistorialEstado}.
 *
 * <p>Esqueleto base. Las queries específicas (findBy*, @Query custom, etc.) se
 * agregarán en sprints posteriores cuando se implementen los CRUDs.</p>
 */
@Repository
public interface HistorialEstadoRepository extends JpaRepository<HistorialEstado, Long> {
}
