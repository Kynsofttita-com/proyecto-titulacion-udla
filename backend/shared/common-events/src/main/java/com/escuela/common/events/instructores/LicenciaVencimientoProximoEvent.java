package com.escuela.common.events.instructores;

import com.escuela.common.events.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

/**
 * Publicado diariamente por MS-Instructores (scheduler) por cada instructor cuya
 * licencia esta proxima a vencer (<=30 dias) o ya vencida.
 *
 * <p>Routing key: {@code instructores.licencia.vencimiento.proximo}
 * Exchange: {@code instructores.exchange}</p>
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LicenciaVencimientoProximoEvent extends BaseEvent {

    public static final String ROUTING_KEY = "instructores.licencia.vencimiento.proximo";

    private Long instructorId;
    private Long usuarioIdInstructor;
    private String cedula;
    private String nombreCompleto;
    private String email;
    private String licenciaCategoria;
    private LocalDate licenciaCaducidad;
    private int diasParaVencer;
    private boolean vencida;
}
