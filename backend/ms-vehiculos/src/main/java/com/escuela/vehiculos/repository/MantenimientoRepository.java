package com.escuela.vehiculos.repository;

import com.escuela.vehiculos.entity.Mantenimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MantenimientoRepository extends JpaRepository<Mantenimiento, Long> {

    Optional<Mantenimiento> findByIdAndDeletedAtIsNull(Long id);

    Optional<Mantenimiento> findByIdAndVehiculoIdAndDeletedAtIsNull(Long id, Long vehiculoId);

    List<Mantenimiento> findByVehiculoIdAndDeletedAtIsNullOrderByFechaDesc(Long vehiculoId);
}
