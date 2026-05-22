package com.escuela.auth.repository;

import com.escuela.auth.entity.ConfiguracionEscuela;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para la entidad {@link ConfiguracionEscuela} (single-tenant: 1 fila).
 */
@Repository
public interface ConfiguracionEscuelaRepository extends JpaRepository<ConfiguracionEscuela, Long> {

    Optional<ConfiguracionEscuela> findByRuc(String ruc);
}
