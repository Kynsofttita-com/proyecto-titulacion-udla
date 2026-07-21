package com.escuela.common.events.instructores;

import com.escuela.common.events.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Evento publicado por MS-Instructores cuando un instructor es eliminado
 * (soft-delete: deletedAt = now).
 *
 * <p>Routing key: {@code instructores.eliminado}
 * Exchange: {@code instructores.exchange}</p>
 *
 * <p>Consumidores:</p>
 * <ul>
 *   <li>MS-Auth: soft-delete del Usuario asociado (busqueda por cedula) para que
 *       el instructor eliminado deje de figurar en la vista "Usuarios del sistema".</li>
 * </ul>
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InstructorEliminadoEvent extends BaseEvent {

    public static final String ROUTING_KEY = "instructores.eliminado";

    private Long instructorId;
    private String cedula;
    /** Motivo opcional registrado por el usuario que ejecuta la baja. */
    private String motivo;
}
