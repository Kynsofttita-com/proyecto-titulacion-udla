package com.escuela.common.events.estudiantes;

import com.escuela.common.events.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Evento publicado por MS-Estudiantes cuando se actualizan datos de un
 * estudiante (PUT /estudiantes/{id}).
 *
 * <p>Routing key: {@code estudiantes.actualizado}
 * Exchange: {@code estudiantes.exchange}</p>
 *
 * <p>Solo se publica si hay cambios efectivos. Los campos opcionales en el
 * payload representan el ESTADO ACTUAL (post-update) de los datos relevantes
 * para los consumidores; no incluyen el diff.</p>
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EstudianteActualizadoEvent extends BaseEvent {

    public static final String ROUTING_KEY = "estudiantes.actualizado";

    private Long estudianteId;
    private String cedula;
    private String email;
    private String nombreCompleto;
    private String estado;
}
