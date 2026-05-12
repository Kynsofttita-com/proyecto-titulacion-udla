package com.escuela.vehiculos.repository;

import com.escuela.vehiculos.entity.Vehiculo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {
    Optional<Vehiculo> findByIdAndDeletedAtIsNull(Long id);
    Page<Vehiculo> findByDeletedAtIsNull(Pageable pageable);
    boolean existsByPlacaAndDeletedAtIsNull(String placa);
}
