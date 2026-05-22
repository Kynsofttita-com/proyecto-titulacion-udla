package com.escuela.auth.repository;

import com.escuela.auth.entity.CategoriaLicencia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoriaLicenciaRepository extends JpaRepository<CategoriaLicencia, Long> {

    Optional<CategoriaLicencia> findByIdAndDeletedAtIsNull(Long id);

    Optional<CategoriaLicencia> findByCodigoAndDeletedAtIsNull(String codigo);

    Page<CategoriaLicencia> findByDeletedAtIsNull(Pageable pageable);
}
