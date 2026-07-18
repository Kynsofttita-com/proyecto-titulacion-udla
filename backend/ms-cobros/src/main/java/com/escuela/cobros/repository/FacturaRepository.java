package com.escuela.cobros.repository;

import com.escuela.cobros.entity.Factura;
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
public interface FacturaRepository extends JpaRepository<Factura, Long> {

    Optional<Factura> findByIdAndDeletedAtIsNull(Long id);

    Page<Factura> findByDeletedAtIsNull(Pageable pageable);

    @Query("SELECT f FROM Factura f WHERE f.estudianteId = :estudianteId AND f.deletedAt IS NULL")
    Page<Factura> findByEstudianteIdAndDeletedAtIsNull(
        @Param("estudianteId") Long estudianteId,
        Pageable pageable
    );

    // SUBSTRING(s, 5) toma desde el char 5 (saltando el prefijo "FAC-") hasta
    // el final, para parsear el correlativo de "FAC-0001" → 1. Antes usaba
    // -4 lo cual en PostgreSQL devolvía toda la cadena y reventaba el CAST.
    @Query("SELECT COALESCE(CAST(MAX(CAST(SUBSTRING(f.numeroFactura, 5) AS INTEGER)) AS LONG), 0) " +
           "FROM Factura f WHERE f.numeroFactura LIKE 'FAC-%'")
    Long findMaxNumeroFactura();

    /**
     * Facturas con saldo pendiente y fecha_vencimiento anterior a hoy.
     * Excluye facturas ANULADAS, PAGADAS y soft-deleted.
     */
    @Query("""
        SELECT f FROM Factura f
        WHERE f.deletedAt IS NULL
          AND f.estado NOT IN ('PAGADA', 'ANULADA')
          AND f.saldo > 0
          AND f.fechaVencimiento < :hoy
        ORDER BY f.fechaVencimiento ASC
    """)
    List<Factura> findAtrasadas(@Param("hoy") LocalDate hoy);
}
