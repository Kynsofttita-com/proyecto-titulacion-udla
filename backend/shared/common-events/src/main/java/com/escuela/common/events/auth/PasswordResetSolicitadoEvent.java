package com.escuela.common.events.auth;

import com.escuela.common.events.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Evento publicado por MS-Auth cuando un usuario solicita recuperar su
 * contrasenia (POST /auth/forgot-password).
 *
 * <p>Routing key: {@code auth.password.reset.solicitado}
 * Exchange: {@code auth.exchange}</p>
 *
 * <p>Consumido por MS-Notificaciones para enviar el email con el link de
 * reset (que contiene el token UUID).</p>
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PasswordResetSolicitadoEvent extends BaseEvent {

    public static final String ROUTING_KEY = "auth.password.reset.solicitado";

    private Long usuarioId;
    private String email;
    private String nombre;
    /** Token UUID de reset. El email contiene un link tipo /reset-password?token={resetToken}. */
    private String resetToken;
    /** Minutos hasta que el token expira. */
    private int expiraEnMinutos;
}
