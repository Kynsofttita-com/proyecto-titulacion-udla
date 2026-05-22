package com.escuela.auth.repository;

import com.escuela.auth.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {

    Optional<Rol> findByNombre(String nombre);

    List<Rol> findByNombreIn(List<String> nombres);

    Optional<Rol> findByIdAndDeletedAtIsNull(Long id);
}
