package com.escuela.cobros.repository;

import com.escuela.cobros.entity.Reconciliacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la entidad {@link Reconciliacion}.
 *
 * <p>Esqueleto base. Las queries específicas (findBy*, @Query custom, etc.) se
 * agregarán en sprints posteriores cuando se implementen los CRUDs.</p>
 */
@Repository
public interface ReconciliacionRepository extends JpaRepository<Reconciliacion, Long> {
}
