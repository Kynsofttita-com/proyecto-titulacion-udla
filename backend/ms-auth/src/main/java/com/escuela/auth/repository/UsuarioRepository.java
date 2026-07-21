package com.escuela.auth.repository;

import com.escuela.auth.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por email. Usado por AuthService (Sprint 4) y por el
     * CRUD de usuarios (Sprint 5).
     */
    Optional<Usuario> findByEmail(String email);

    /** Para el CRUD: solo usuarios no borrados. */
    Optional<Usuario> findByIdAndDeletedAtIsNull(Long id);

    Optional<Usuario> findByEmailAndDeletedAtIsNull(String email);

    Optional<Usuario> findByCedulaAndDeletedAtIsNull(String cedula);

    Page<Usuario> findByDeletedAtIsNull(Pageable pageable);

    /** Filtro por activo/locked. */
    @Query("""
        SELECT u FROM Usuario u
        WHERE u.deletedAt IS NULL
          AND (:activo IS NULL OR u.activo = :activo)
          AND (:locked IS NULL OR u.locked = :locked)
    """)
    Page<Usuario> buscar(@Param("activo") Boolean activo,
                          @Param("locked") Boolean locked,
                          Pageable pageable);
}
