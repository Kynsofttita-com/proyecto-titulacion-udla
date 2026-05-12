package com.escuela.common.events.estudiantes;

import com.escuela.common.events.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Evento publicado por MS-Estudiantes cuando un estudiante es eliminado
 * (soft-delete: deletedAt = now).
 *
 * <p>Routing key: {@code estudiantes.eliminado}
 * Exchange: {@code estudiantes.exchange}</p>
 *
 * <p>El registro no se borra fisicamente; los consumidores deben marcar el
 * estudiante como inactivo en sus respectivos almacenes (audit log, caches).</p>
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EstudianteEliminadoEvent extends BaseEvent {

    public static final String ROUTING_KEY = "estudiantes.eliminado";

    private Long estudianteId;
    private String cedula;
    /** Motivo opcional registrado por el usuario que ejecuta la baja. */
    private String motivo;
}
