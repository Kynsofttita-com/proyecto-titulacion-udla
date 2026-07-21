package com.escuela.cobros.repository;

import com.escuela.cobros.entity.CuentaContable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CuentaContableRepository extends JpaRepository<CuentaContable, Long> {

    List<CuentaContable> findAllByOrderByNombreAsc();

    List<CuentaContable> findByActivoTrueOrderByNombreAsc();

    @Query("SELECT c FROM CuentaContable c WHERE LOWER(c.nombre) = LOWER(:nombre)")
    Optional<CuentaContable> findByNombreIgnoreCase(String nombre);
}
