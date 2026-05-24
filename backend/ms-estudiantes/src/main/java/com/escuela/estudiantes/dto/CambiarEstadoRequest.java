package com.escuela.estudiantes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Payload para cambio manual de estado academico de un estudiante.
 *
 * <p>Los cambios automaticos (via eventos pago.registrado / asignacion.creada)
 * NO usan este endpoint; ver {@code EstudianteEstadoService}.</p>
 *
 * <p>Transiciones manuales validas las arbitra el service.</p>
 */
public record CambiarEstadoRequest(

        @NotBlank
        @Pattern(regexp = "^(MATRICULADO|CURSANDO|COMPLETADO|RETIRADO)$",
                message = "Estado debe ser MATRICULADO, CURSANDO, COMPLETADO o RETIRADO")
        String estado,

        @Size(max = 500, message = "El motivo no debe exceder 500 caracteres")
        String motivo

) {}
