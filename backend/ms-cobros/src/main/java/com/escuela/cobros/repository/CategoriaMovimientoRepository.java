package com.escuela.cobros.repository;

import com.escuela.cobros.entity.CategoriaMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaMovimientoRepository extends JpaRepository<CategoriaMovimiento, Long> {

    List<CategoriaMovimiento> findAllByOrderByTipoAscNombreAsc();

    List<CategoriaMovimiento> findByTipoAndActivoTrueOrderByNombreAsc(String tipo);

    List<CategoriaMovimiento> findByActivoTrueOrderByTipoAscNombreAsc();

    Optional<CategoriaMovimiento> findByCodigo(String codigo);
}
