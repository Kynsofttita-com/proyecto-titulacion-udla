package com.escuela.estudiantes.repository;

import com.escuela.estudiantes.entity.ProgresoAcademico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProgresoAcademicoRepository extends JpaRepository<ProgresoAcademico, Long> {

    Optional<ProgresoAcademico> findByEstudianteId(Long estudianteId);
}
