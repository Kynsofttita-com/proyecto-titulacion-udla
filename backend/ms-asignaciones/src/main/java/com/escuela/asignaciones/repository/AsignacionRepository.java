package com.escuela.asignaciones.repository;

import com.escuela.asignaciones.entity.Asignacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface AsignacionRepository extends JpaRepository<Asignacion, Long> {
    Optional<Asignacion> findByIdAndDeletedAtIsNull(Long id);
    Page<Asignacion> findByDeletedAtIsNull(Pageable pageable);
    long countByInstructorIdAndFechaAndEstadoAndDeletedAtIsNull(Long instructorId, LocalDate fecha, String estado);
    long countByVehiculoIdAndFechaAndEstadoAndDeletedAtIsNull(Long vehiculoId, LocalDate fecha, String estado);
}
