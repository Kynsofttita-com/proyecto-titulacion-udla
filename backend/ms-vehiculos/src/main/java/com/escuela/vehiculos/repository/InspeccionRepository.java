package com.escuela.vehiculos.repository;

import com.escuela.vehiculos.entity.Inspeccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la entidad {@link Inspeccion}.
 *
 * <p>Esqueleto base. Las queries específicas (findBy*, @Query custom, etc.) se
 * agregarán en sprints posteriores cuando se implementen los CRUDs.</p>
 */
@Repository
public interface InspeccionRepository extends JpaRepository<Inspeccion, Long> {
}
