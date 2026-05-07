package com.escuela.cobros.repository;

import com.escuela.cobros.entity.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la entidad {@link Factura}.
 *
 * <p>Esqueleto base. Las queries específicas (findBy*, @Query custom, etc.) se
 * agregarán en sprints posteriores cuando se implementen los CRUDs.</p>
 */
@Repository
public interface FacturaRepository extends JpaRepository<Factura, Long> {
}
