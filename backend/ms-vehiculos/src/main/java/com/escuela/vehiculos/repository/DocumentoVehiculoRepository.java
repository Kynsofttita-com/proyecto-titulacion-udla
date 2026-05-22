package com.escuela.vehiculos.repository;

import com.escuela.vehiculos.entity.DocumentoVehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentoVehiculoRepository extends JpaRepository<DocumentoVehiculo, Long> {

    Optional<DocumentoVehiculo> findByIdAndDeletedAtIsNull(Long id);

    Optional<DocumentoVehiculo> findByIdAndVehiculoIdAndDeletedAtIsNull(Long id, Long vehiculoId);

    List<DocumentoVehiculo> findByVehiculoIdAndDeletedAtIsNullOrderByFechaVencimientoAsc(Long vehiculoId);
}
