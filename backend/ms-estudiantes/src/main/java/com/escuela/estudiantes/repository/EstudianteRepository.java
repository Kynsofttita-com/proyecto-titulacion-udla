package com.escuela.estudiantes.repository;

import com.escuela.estudiantes.entity.Estudiante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para {@link Estudiante}.
 *
 * <p>Extiende {@link JpaSpecificationExecutor} para soportar filtros dinamicos
 * (search, estado) via {@code EstudianteSpecifications}.</p>
 *
 * <p>Todas las queries excluyen registros con {@code deleted_at IS NOT NULL}
 * (soft delete).</p>
 */
@Repository
public interface EstudianteRepository
        extends JpaRepository<Estudiante, Long>,
                JpaSpecificationExecutor<Estudiante> {

    Optional<Estudiante> findByCedulaAndDeletedAtIsNull(String cedula);

    Optional<Estudiante> findByEmailAndDeletedAtIsNull(String email);

    Optional<Estudiante> findByIdAndDeletedAtIsNull(Long id);

    Optional<Estudiante> findByUsuarioIdAndDeletedAtIsNull(Long usuarioId);

    boolean existsByCedulaAndDeletedAtIsNull(String cedula);

    boolean existsByEmailAndDeletedAtIsNull(String email);

    /** Incluye soft-deleted. Usado para detectar colisiones con la UNIQUE global de cedula/email. */
    Optional<Estudiante> findByCedula(String cedula);

    Optional<Estudiante> findByEmail(String email);
}
