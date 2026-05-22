package com.escuela.instructores.repository;

import com.escuela.instructores.entity.HorarioTrabajo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HorarioTrabajoRepository extends JpaRepository<HorarioTrabajo, Long> {

    Optional<HorarioTrabajo> findByIdAndDeletedAtIsNull(Long id);

    Optional<HorarioTrabajo> findByIdAndInstructorIdAndDeletedAtIsNull(Long id, Long instructorId);

    List<HorarioTrabajo> findByInstructorIdAndDeletedAtIsNullOrderByFechaDesc(Long instructorId);

    List<HorarioTrabajo> findByInstructorIdAndFechaAndDeletedAtIsNull(Long instructorId, LocalDate fecha);
}
