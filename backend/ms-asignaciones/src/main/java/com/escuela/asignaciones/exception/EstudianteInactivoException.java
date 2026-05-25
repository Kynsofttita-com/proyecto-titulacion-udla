package com.escuela.asignaciones.exception;

import com.escuela.common.exceptions.NegocioException;
import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando se intenta crear una asignacion para un estudiante que
 * no puede recibir clases. Causas tipicas:
 * <ul>
 *   <li>{@code PRE_MATRICULADO}: aun no completa el pago (Sprint 9 ext:
 *       regla "no clases con saldo"). Cobrar el saldo y reintentar.</li>
 *   <li>{@code COMPLETADO}: el estudiante ya termino su curso.</li>
 *   <li>{@code RETIRADO}: el estudiante abandono. Reactivar primero
 *       (PATCH /estudiantes/&lt;id&gt;/estado).</li>
 * </ul>
 */
public class EstudianteInactivoException extends NegocioException {

    public EstudianteInactivoException(Long estudianteId) {
        super(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "ESTUDIANTE_NO_PUEDE_RECIBIR_CLASES",
                "El estudiante " + estudianteId + " no puede recibir asignaciones. "
                        + "Solo se permite crear clases para estudiantes MATRICULADOS o CURSANDO. "
                        + "Si esta en PRE_MATRICULADO, debe completar el pago primero."
        );
    }
}
