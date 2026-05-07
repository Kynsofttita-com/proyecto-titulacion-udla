package com.escuela.common.events.auth;

import com.escuela.common.events.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Evento publicado por MS-Auth cuando se crea un usuario.
 *
 * <p>Routing key: {@code auth.usuario.creado}
 * Exchange: {@code auth.exchange}</p>
 *
 * <p>Consumido por MS-Notificaciones (para crear preferencias por defecto y
 * enviar email de bienvenida) y por MS-Estudiantes/MS-Instructores cuando se
 * vincula un usuario a un perfil de dominio.</p>
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UsuarioCreadoEvent extends BaseEvent {

    /** Routing key estandar usado al publicar este evento. */
    public static final String ROUTING_KEY = "auth.usuario.creado";

    private Long usuarioId;
    private String email;
    private String nombre;
    private String apellido;
}
