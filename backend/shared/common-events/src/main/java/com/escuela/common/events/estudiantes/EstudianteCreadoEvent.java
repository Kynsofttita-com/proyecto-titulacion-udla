package com.escuela.common.events.estudiantes;

import com.escuela.common.events.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Evento publicado por MS-Estudiantes cuando se crea un nuevo estudiante.
 *
 * <p>Routing key: {@code estudiantes.creado}
 * Exchange: {@code estudiantes.exchange}</p>
 *
 * <p>Consumidores:</p>
 * <ul>
 *   <li>MS-Auth: registra entrada en {@code audit_log}.</li>
 *   <li>MS-Reportes: actualiza contadores agregados.</li>
 * </ul>
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EstudianteCreadoEvent extends BaseEvent {

    public static final String ROUTING_KEY = "estudiantes.creado";

    private Long estudianteId;
    private String cedula;
    private String email;
    private String nombreCompleto;
    private String estado;
}
