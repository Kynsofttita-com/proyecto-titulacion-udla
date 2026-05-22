package com.escuela.instructores.repository;

import com.escuela.instructores.entity.Disponibilidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DisponibilidadRepository extends JpaRepository<Disponibilidad, Long> {

    Optional<Disponibilidad> findByIdAndDeletedAtIsNull(Long id);

    Optional<Disponibilidad> findByIdAndInstructorIdAndDeletedAtIsNull(Long id, Long instructorId);

    List<Disponibilidad> findByInstructorIdAndDeletedAtIsNullOrderByDiaSemanaAscHoraInicioAsc(Long instructorId);

    List<Disponibilidad> findByInstructorIdAndDiaSemanaAndDeletedAtIsNullOrderByHoraInicioAsc(
            Long instructorId, Short diaSemana);
}
