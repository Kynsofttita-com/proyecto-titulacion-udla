package com.escuela.common.events.auth;

import com.escuela.common.events.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * Evento publicado por MS-Auth cuando un usuario es bloqueado tras 3 intentos
 * de login fallidos consecutivos.
 *
 * <p>Routing key: {@code auth.usuario.bloqueado}
 * Exchange: {@code auth.exchange}</p>
 *
 * <p>Consumido por MS-Notificaciones para enviar email "tu cuenta fue bloqueada
 * por 15 minutos por intentos fallidos".</p>
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UsuarioBloqueadoEvent extends BaseEvent {

    public static final String ROUTING_KEY = "auth.usuario.bloqueado";

    private Long usuarioId;
    private String email;
    private String nombre;
    private Instant lockUntil;
    /** Numero de intentos fallidos que dispararon el lockout. */
    private int intentosFallidos;
}
