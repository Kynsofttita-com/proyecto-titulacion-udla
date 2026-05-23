package com.escuela.cobros.repository;

import com.escuela.cobros.entity.Factura;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Long> {

    Optional<Factura> findByIdAndDeletedAtIsNull(Long id);

    Page<Factura> findByDeletedAtIsNull(Pageable pageable);

    @Query("SELECT f FROM Factura f WHERE f.estudianteId = :estudianteId AND f.deletedAt IS NULL")
    Page<Factura> findByEstudianteIdAndDeletedAtIsNull(
        @Param("estudianteId") Long estudianteId,
        Pageable pageable
    );

    @Query("SELECT COALESCE(CAST(MAX(CAST(SUBSTRING(f.numeroFactura, -4) AS INTEGER)) AS LONG), 0) " +
           "FROM Factura f WHERE f.numeroFactura LIKE 'FAC-%'")
    Long findMaxNumeroFactura();
}
