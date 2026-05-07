package com.escuela.auth.repository;

import com.escuela.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByJti(UUID jti);

    /** Revoca todos los refresh tokens activos de un usuario (usado en logout o cambio de password). */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revocado = true, rt.revocadoAt = :now " +
           "WHERE rt.usuarioId = :usuarioId AND rt.revocado = false")
    int revocarTodosDelUsuario(@Param("usuarioId") Long usuarioId,
                               @Param("now") LocalDateTime now);
}
