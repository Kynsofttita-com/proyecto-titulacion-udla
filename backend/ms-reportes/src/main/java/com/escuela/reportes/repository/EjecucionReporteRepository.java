package com.escuela.reportes.repository;

import com.escuela.reportes.entity.EjecucionReporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la entidad {@link EjecucionReporte}.
 *
 * <p>Esqueleto base. Las queries específicas (findBy*, @Query custom, etc.) se
 * agregarán en sprints posteriores cuando se implementen los CRUDs.</p>
 */
@Repository
public interface EjecucionReporteRepository extends JpaRepository<EjecucionReporte, Long> {
}
