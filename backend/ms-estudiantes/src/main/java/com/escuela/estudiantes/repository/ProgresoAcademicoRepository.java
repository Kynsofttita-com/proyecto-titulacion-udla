package com.escuela.estudiantes.repository;

import com.escuela.estudiantes.entity.ProgresoAcademico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la entidad {@link ProgresoAcademico}.
 *
 * <p>Esqueleto base. Las queries específicas (findBy*, @Query custom, etc.) se
 * agregarán en sprints posteriores cuando se implementen los CRUDs.</p>
 */
@Repository
public interface ProgresoAcademicoRepository extends JpaRepository<ProgresoAcademico, Long> {
}
