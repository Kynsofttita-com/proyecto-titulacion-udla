package com.escuela.instructores.repository;

import com.escuela.instructores.entity.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la entidad {@link Instructor}.
 *
 * <p>Esqueleto base. Las queries específicas (findBy*, @Query custom, etc.) se
 * agregarán en sprints posteriores cuando se implementen los CRUDs.</p>
 */
@Repository
public interface InstructorRepository extends JpaRepository<Instructor, Long> {
}
