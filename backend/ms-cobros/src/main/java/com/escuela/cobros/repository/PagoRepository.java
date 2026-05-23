package com.escuela.cobros.repository;

import com.escuela.cobros.entity.Pago;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {

    @Query("SELECT p FROM Pago p WHERE p.factura.id = :facturaId")
    Page<Pago> findByFacturaId(@Param("facturaId") Long facturaId, Pageable pageable);

    @Query("SELECT p FROM Pago p INNER JOIN p.factura f " +
           "WHERE f.estudianteId = :estudianteId")
    Page<Pago> findByEstudianteId(@Param("estudianteId") Long estudianteId, Pageable pageable);
}
