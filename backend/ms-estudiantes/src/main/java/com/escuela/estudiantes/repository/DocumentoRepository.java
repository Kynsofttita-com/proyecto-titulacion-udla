package com.escuela.estudiantes.repository;

import com.escuela.estudiantes.entity.Documento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentoRepository extends JpaRepository<Documento, Long> {

    Optional<Documento> findByIdAndDeletedAtIsNull(Long id);

    List<Documento> findByEstudianteIdAndDeletedAtIsNullOrderByFechaSubidaDesc(Long estudianteId);

    Optional<Documento> findByIdAndEstudianteIdAndDeletedAtIsNull(Long id, Long estudianteId);
}
