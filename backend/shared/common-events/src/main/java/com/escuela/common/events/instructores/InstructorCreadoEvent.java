package com.escuela.common.events.instructores;

import com.escuela.common.events.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Evento publicado por MS-Instructores cuando se crea un nuevo instructor.
 *
 * <p>Routing key: {@code instructores.creado}
 * Exchange: {@code instructores.exchange}</p>
 *
 * <p>Consumidores:</p>
 * <ul>
 *   <li>MS-Auth: crea automaticamente un Usuario con rol INSTRUCTOR y publica
 *       {@code auth.usuario.creado} para que MS-Instructores enlace usuario_id.</li>
 *   <li>MS-Reportes: actualiza contadores agregados.</li>
 * </ul>
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InstructorCreadoEvent extends BaseEvent {

    public static final String ROUTING_KEY = "instructores.creado";

    private Long instructorId;
    private String cedula;
    private String email;
    private String nombre;
    private String apellido;
}
