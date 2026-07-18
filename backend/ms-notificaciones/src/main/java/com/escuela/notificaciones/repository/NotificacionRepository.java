package com.escuela.notificaciones.repository;

import com.escuela.notificaciones.entity.Notificacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    @Query("SELECT n FROM Notificacion n WHERE n.usuarioId = :usuarioId AND n.deletedAt IS NULL ORDER BY n.fechaCreacion DESC")
    Page<Notificacion> findByUsuarioId(@Param("usuarioId") Long usuarioId, Pageable pageable);

    @Query("SELECT n FROM Notificacion n WHERE n.usuarioId = :usuarioId AND n.leida = :leida AND n.deletedAt IS NULL ORDER BY n.fechaCreacion DESC")
    Page<Notificacion> findByUsuarioIdAndLeida(@Param("usuarioId") Long usuarioId, @Param("leida") Boolean leida, Pageable pageable);

    @Query("SELECT n FROM Notificacion n WHERE n.usuarioId = :usuarioId AND n.tipo = :tipo AND n.deletedAt IS NULL ORDER BY n.fechaCreacion DESC")
    Page<Notificacion> findByUsuarioIdAndTipo(@Param("usuarioId") Long usuarioId, @Param("tipo") String tipo, Pageable pageable);

    @Query("SELECT n FROM Notificacion n WHERE n.usuarioId = :usuarioId AND n.prioridad = :prioridad AND n.deletedAt IS NULL ORDER BY n.fechaCreacion DESC")
    Page<Notificacion> findByUsuarioIdAndPrioridad(@Param("usuarioId") Long usuarioId, @Param("prioridad") String prioridad, Pageable pageable);

    @Query("SELECT n FROM Notificacion n WHERE n.usuarioId = :usuarioId AND n.leida = :leida AND n.tipo = :tipo AND n.prioridad = :prioridad AND n.deletedAt IS NULL ORDER BY n.fechaCreacion DESC")
    Page<Notificacion> findByUsuarioIdWithFilters(
        @Param("usuarioId") Long usuarioId,
        @Param("leida") Boolean leida,
        @Param("tipo") String tipo,
        @Param("prioridad") String prioridad,
        Pageable pageable
    );

    /**
     * Marca todas las notificaciones no leidas de un usuario como leidas.
     * @return numero de filas afectadas
     */
    @Modifying
    @Query("UPDATE Notificacion n SET n.leida = TRUE, n.fechaLectura = :ahora " +
           "WHERE n.usuarioId = :usuarioId AND n.leida = FALSE AND n.deletedAt IS NULL")
    int marcarTodasComoLeidas(@Param("usuarioId") Long usuarioId, @Param("ahora") LocalDateTime ahora);

    /**
     * Soft-delete de todas las notificaciones no eliminadas de un usuario.
     * @return numero de filas afectadas
     */
    @Modifying
    @Query("UPDATE Notificacion n SET n.deletedAt = :ahora " +
           "WHERE n.usuarioId = :usuarioId AND n.deletedAt IS NULL")
    int eliminarTodasPorUsuario(@Param("usuarioId") Long usuarioId, @Param("ahora") LocalDateTime ahora);
}
