package com.escuela.common.events.estudiantes;

import com.escuela.common.events.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

/**
 * Publicado por MS-Estudiantes cuando el estudiante llega al 100% de horas
 * requeridas por su tipo de curso (minutos_completados >= duracionTotalHoras*60).
 *
 * <p>Routing key: {@code estudiantes.curso.completado}
 * Exchange: {@code estudiantes.exchange}</p>
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CursoCompletadoEvent extends BaseEvent {

    public static final String ROUTING_KEY = "estudiantes.curso.completado";

    private Long estudianteId;
    private Long usuarioIdEstudiante;
    private String cedula;
    private String nombreCompleto;
    private String email;
    private String tipoCursoNombre;
    private int horasCompletadas;
    private int horasRequeridas;
    private LocalDate fechaCompletado;
}
