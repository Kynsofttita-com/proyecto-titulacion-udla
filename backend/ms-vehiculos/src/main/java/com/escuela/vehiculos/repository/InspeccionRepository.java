package com.escuela.vehiculos.repository;

import com.escuela.vehiculos.entity.Inspeccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InspeccionRepository extends JpaRepository<Inspeccion, Long> {

    Optional<Inspeccion> findByIdAndDeletedAtIsNull(Long id);

    Optional<Inspeccion> findByIdAndVehiculoIdAndDeletedAtIsNull(Long id, Long vehiculoId);

    List<Inspeccion> findByVehiculoIdAndDeletedAtIsNullOrderByFechaDesc(Long vehiculoId);
}
