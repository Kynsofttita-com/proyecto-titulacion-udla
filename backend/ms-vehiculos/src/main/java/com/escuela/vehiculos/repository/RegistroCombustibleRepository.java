package com.escuela.vehiculos.repository;

import com.escuela.vehiculos.entity.RegistroCombustible;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RegistroCombustibleRepository extends JpaRepository<RegistroCombustible, Long> {

    Optional<RegistroCombustible> findByIdAndVehiculoId(Long id, Long vehiculoId);

    Page<RegistroCombustible> findByVehiculoIdOrderByFechaDesc(Long vehiculoId, Pageable pageable);
}
