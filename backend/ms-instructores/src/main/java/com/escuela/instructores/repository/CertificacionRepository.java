package com.escuela.instructores.repository;

import com.escuela.instructores.entity.Certificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CertificacionRepository extends JpaRepository<Certificacion, Long> {

    Optional<Certificacion> findByIdAndDeletedAtIsNull(Long id);

    Optional<Certificacion> findByIdAndInstructorIdAndDeletedAtIsNull(Long id, Long instructorId);

    List<Certificacion> findByInstructorIdAndDeletedAtIsNullOrderByFechaObtencionDesc(Long instructorId);
}
