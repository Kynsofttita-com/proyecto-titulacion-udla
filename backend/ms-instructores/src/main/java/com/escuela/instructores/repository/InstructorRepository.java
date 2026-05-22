package com.escuela.instructores.repository;

import com.escuela.instructores.entity.Instructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InstructorRepository extends JpaRepository<Instructor, Long> {

    Optional<Instructor> findByIdAndDeletedAtIsNull(Long id);

    Optional<Instructor> findByCedulaAndDeletedAtIsNull(String cedula);

    Optional<Instructor> findByEmailAndDeletedAtIsNull(String email);

    Optional<Instructor> findByLicenciaNumeroAndDeletedAtIsNull(String licenciaNumero);

    Page<Instructor> findByDeletedAtIsNull(Pageable pageable);

    @Query("""
        SELECT i FROM Instructor i
        WHERE i.deletedAt IS NULL
          AND (:estado IS NULL OR i.estado = :estado)
          AND (:search IS NULL OR
               LOWER(i.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR
               LOWER(i.apellido) LIKE LOWER(CONCAT('%', :search, '%')) OR
               i.cedula LIKE CONCAT('%', :search, '%') OR
               LOWER(i.email) LIKE LOWER(CONCAT('%', :search, '%')))
    """)
    Page<Instructor> buscar(@Param("search") String search,
                             @Param("estado") String estado,
                             Pageable pageable);
}
