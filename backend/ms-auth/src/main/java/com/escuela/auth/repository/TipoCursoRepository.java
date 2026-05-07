package com.escuela.auth.repository;

import com.escuela.auth.entity.TipoCurso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la entidad {@link TipoCurso}.
 *
 * <p>Esqueleto base. Las queries específicas (findBy*, @Query custom, etc.) se
 * agregarán en sprints posteriores cuando se implementen los CRUDs.</p>
 */
@Repository
public interface TipoCursoRepository extends JpaRepository<TipoCurso, Long> {
}
