package com.escuela.estudiantes.repository;

import com.escuela.estudiantes.entity.ContactoEmergencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContactoEmergenciaRepository extends JpaRepository<ContactoEmergencia, Long> {

    Optional<ContactoEmergencia> findByIdAndDeletedAtIsNull(Long id);

    Optional<ContactoEmergencia> findByIdAndEstudianteIdAndDeletedAtIsNull(Long id, Long estudianteId);

    List<ContactoEmergencia> findByEstudianteIdAndDeletedAtIsNull(Long estudianteId);

    long countByEstudianteIdAndDeletedAtIsNull(Long estudianteId);
}
