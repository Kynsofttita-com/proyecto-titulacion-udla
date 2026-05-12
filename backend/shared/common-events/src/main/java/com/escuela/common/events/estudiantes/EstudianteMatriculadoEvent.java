package com.escuela.common.events.estudiantes;

import com.escuela.common.events.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

/**
 * Evento publicado cuando el estado de un estudiante pasa a {@code ACTIVO},
 * lo que en el dominio se entiende como "matriculado oficialmente".
 *
 * <p>Routing key: {@code estudiantes.matriculado}
 * Exchange: {@code estudiantes.exchange}</p>
 *
 * <p>Distinto de {@code EstudianteActualizadoEvent} porque dispara flujos
 * especificos (envio de correo de bienvenida con cronograma de clases,
 * apertura de cuenta de cobros, etc.).</p>
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EstudianteMatriculadoEvent extends BaseEvent {

    public static final String ROUTING_KEY = "estudiantes.matriculado";

    private Long estudianteId;
    private String cedula;
    private String email;
    private String nombreCompleto;
    private LocalDate fechaMatricula;
}
