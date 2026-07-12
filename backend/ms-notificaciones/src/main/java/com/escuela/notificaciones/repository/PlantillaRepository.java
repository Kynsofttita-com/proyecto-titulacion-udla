package com.escuela.notificaciones.repository;

import com.escuela.notificaciones.entity.Plantilla;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlantillaRepository extends JpaRepository<Plantilla, Long> {

    Optional<Plantilla> findByCodigoAndDeletedAtIsNull(String codigo);

    Page<Plantilla> findByActivaTrueAndDeletedAtIsNull(Pageable pageable);

    Page<Plantilla> findByDeletedAtIsNull(Pageable pageable);

    boolean existsByCodigoAndDeletedAtIsNull(String codigo);

}
