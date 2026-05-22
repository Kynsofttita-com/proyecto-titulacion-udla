package com.escuela.auth.repository;

import com.escuela.auth.entity.ConceptoFacturacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConceptoFacturacionRepository extends JpaRepository<ConceptoFacturacion, Long> {

    Optional<ConceptoFacturacion> findByIdAndDeletedAtIsNull(Long id);

    Optional<ConceptoFacturacion> findByNombreAndDeletedAtIsNull(String nombre);

    Page<ConceptoFacturacion> findByDeletedAtIsNull(Pageable pageable);
}
