package com.escuela.auth.repository;

import com.escuela.auth.entity.PlantillaEmail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlantillaEmailRepository extends JpaRepository<PlantillaEmail, Long> {

    Optional<PlantillaEmail> findByIdAndDeletedAtIsNull(Long id);

    Optional<PlantillaEmail> findByCodigoAndDeletedAtIsNull(String codigo);

    Page<PlantillaEmail> findByDeletedAtIsNull(Pageable pageable);
}
