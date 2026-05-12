package com.escuela.cobros.repository;

import com.escuela.cobros.entity.Cobro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CobroRepository extends JpaRepository<Cobro, Long> {
    Optional<Cobro> findByIdAndDeletedAtIsNull(Long id);
    Page<Cobro> findByDeletedAtIsNull(Pageable pageable);
    Page<Cobro> findByEstadoAndDeletedAtIsNull(String estado, Pageable pageable);
}
