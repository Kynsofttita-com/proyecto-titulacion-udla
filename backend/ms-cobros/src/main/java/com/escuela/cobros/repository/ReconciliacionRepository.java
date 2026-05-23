package com.escuela.cobros.repository;

import com.escuela.cobros.entity.Reconciliacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ReconciliacionRepository extends JpaRepository<Reconciliacion, Long> {

    Optional<Reconciliacion> findByFecha(LocalDate fecha);

    Page<Reconciliacion> findAllByOrderByFechaDesc(Pageable pageable);
}
