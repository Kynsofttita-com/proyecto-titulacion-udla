package com.escuela.vehiculos.repository;

import com.escuela.vehiculos.entity.Vehiculo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {

    Optional<Vehiculo> findByIdAndDeletedAtIsNull(Long id);

    Optional<Vehiculo> findByPlacaAndDeletedAtIsNull(String placa);

    Page<Vehiculo> findByDeletedAtIsNull(Pageable pageable);

    boolean existsByPlacaAndDeletedAtIsNull(String placa);

    /** Busqueda con filtros opcionales (search, estado). */
    @Query("""
        SELECT v FROM Vehiculo v
        WHERE v.deletedAt IS NULL
          AND (:search IS NULL OR :search = '' OR
               LOWER(v.placa)  LIKE LOWER(CONCAT('%', :search, '%')) OR
               LOWER(v.marca)  LIKE LOWER(CONCAT('%', :search, '%')) OR
               LOWER(v.modelo) LIKE LOWER(CONCAT('%', :search, '%')))
          AND (:estado IS NULL OR :estado = '' OR v.estado = :estado)
    """)
    Page<Vehiculo> buscar(@Param("search") String search,
                          @Param("estado") String estado,
                          Pageable pageable);

    /** SOAT vencido o por vencer en {@code limite} (inclusive). */
    @Query("""
        SELECT v FROM Vehiculo v
        WHERE v.deletedAt IS NULL
          AND v.soatVencimiento IS NOT NULL
          AND v.soatVencimiento <= :limite
        ORDER BY v.soatVencimiento ASC
    """)
    List<Vehiculo> findConSoatPorVencer(@Param("limite") LocalDate limite);
}
