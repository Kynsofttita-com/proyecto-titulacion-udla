package com.escuela.auth.repository;

import com.escuela.auth.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por email. Devuelve {@code Optional.empty()} si no existe
     * o esta soft-deleted.
     */
    Optional<Usuario> findByEmail(String email);
}
