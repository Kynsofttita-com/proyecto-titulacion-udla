package com.escuela.cobros.repository;

import com.escuela.cobros.entity.FacturaCuota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacturaCuotaRepository extends JpaRepository<FacturaCuota, Long> {

    List<FacturaCuota> findByFacturaIdOrderByNumeroCuotaAsc(Long facturaId);

    /**
     * Devuelve la cuota más antigua aún no saldada (estado PENDIENTE o PARCIAL),
     * usada para aplicar el siguiente pago de crédito.
     */
    Optional<FacturaCuota> findFirstByFacturaIdAndEstadoInOrderByNumeroCuotaAsc(
            Long facturaId, List<String> estados);
}
