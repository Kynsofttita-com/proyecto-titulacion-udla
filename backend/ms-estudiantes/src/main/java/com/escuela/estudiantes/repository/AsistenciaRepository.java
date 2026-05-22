package com.escuela.estudiantes.repository;

import com.escuela.estudiantes.entity.Asistencia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {

    Page<Asistencia> findByEstudianteIdOrderByFechaClaseDesc(Long estudianteId, Pageable pageable);

    Optional<Asistencia> findByIdAndEstudianteId(Long id, Long estudianteId);

    Optional<Asistencia> findByEstudianteIdAndAsignacionId(Long estudianteId, Long asignacionId);

    long countByEstudianteIdAndAsistio(Long estudianteId, Boolean asistio);
}
