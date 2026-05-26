package com.escuela.vehiculos.repository;

import com.escuela.vehiculos.entity.TipoCombustible;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TipoCombustibleRepository extends JpaRepository<TipoCombustible, Long> {

    List<TipoCombustible> findAllByOrderByCodigoAsc();

    List<TipoCombustible> findByActivoTrueOrderByCodigoAsc();

    Optional<TipoCombustible> findByCodigo(String codigo);
}
