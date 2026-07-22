package com.escuela.cobros.repository;

import com.escuela.cobros.entity.MovimientoContable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MovimientoContableRepository extends JpaRepository<MovimientoContable, Long> {

    /**
     * Filtro flexible: cualquier parametro null se ignora.
     * cuentaId, categoriaId, tipo, fechaInicio y fechaFin son opcionales.
     * anulado=false por defecto para no incluir anulados.
     */
    @Query("""
        SELECT m FROM MovimientoContable m
        WHERE m.anulado = false
          AND (:cuentaId IS NULL OR m.cuenta.id = :cuentaId)
          AND (:categoriaId IS NULL OR m.categoria.id = :categoriaId)
          AND (:tipo IS NULL OR m.tipo = :tipo)
          AND (:vehiculoId IS NULL OR m.vehiculoId = :vehiculoId)
          AND (:pagadoAId IS NULL OR m.pagadoAId = :pagadoAId)
          AND (:fechaInicio IS NULL OR m.fecha >= :fechaInicio)
          AND (:fechaFin IS NULL OR m.fecha <= :fechaFin)
        ORDER BY m.fecha DESC, m.id DESC
    """)
    Page<MovimientoContable> buscarConFiltros(
            Long cuentaId,
            Long categoriaId,
            String tipo,
            Long vehiculoId,
            Long pagadoAId,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            Pageable pageable
    );

    /** Suma de gastos NO anulados de un vehiculo por codigo de categoria (ej: COMBUSTIBLE). */
    @Query("""
        SELECT COALESCE(SUM(m.monto), 0) FROM MovimientoContable m
        WHERE m.vehiculoId = :vehiculoId
          AND m.categoria.codigo = :codigoCategoria
          AND m.anulado = false
    """)
    BigDecimal sumaGastosPorVehiculoYCategoria(Long vehiculoId, String codigoCategoria);

    /** Suma de ingresos NO anulados de una cuenta. */
    @Query("""
        SELECT COALESCE(SUM(m.monto), 0) FROM MovimientoContable m
        WHERE m.cuenta.id = :cuentaId
          AND m.tipo = 'INGRESO'
          AND m.anulado = false
    """)
    BigDecimal sumaIngresosPorCuenta(Long cuentaId);

    /** Suma de gastos NO anulados de una cuenta. */
    @Query("""
        SELECT COALESCE(SUM(m.monto), 0) FROM MovimientoContable m
        WHERE m.cuenta.id = :cuentaId
          AND m.tipo = 'GASTO'
          AND m.anulado = false
    """)
    BigDecimal sumaGastosPorCuenta(Long cuentaId);

    Optional<MovimientoContable> findByPagoId(Long pagoId);

    List<MovimientoContable> findByCategoriaIdAndAnuladoFalse(Long categoriaId);

    Optional<MovimientoContable> findByRegistroCombustibleIdAndAnuladoFalse(Long registroCombustibleId);

    Optional<MovimientoContable> findByMantenimientoIdAndAnuladoFalse(Long mantenimientoId);
}
