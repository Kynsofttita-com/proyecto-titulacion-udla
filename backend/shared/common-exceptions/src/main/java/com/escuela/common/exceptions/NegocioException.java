package com.escuela.common.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Excepción base para errores de lógica de negocio en todos los microservicios.
 * Cada subclase debe especificar el código HTTP, código de error y mensaje.
 *
 * <p>Ejemplo de uso:</p>
 * <pre>
 * throw new NegocioException(
 *     HttpStatus.NOT_FOUND,
 *     "ESTUDIANTE_NO_ENCONTRADO",
 *     "No existe estudiante con id " + id
 * );
 * </pre>
 */
@Getter
public class NegocioException extends RuntimeException {

    /** Código HTTP a devolver al cliente (404, 409, 400, etc.). */
    private final HttpStatus status;

    /** Código de error de negocio (ej: "CEDULA_DUPLICADA"). */
    private final String codigo;

    public NegocioException(HttpStatus status, String codigo, String mensaje) {
        super(mensaje);
        this.status = status;
        this.codigo = codigo;
    }

    public NegocioException(HttpStatus status, String codigo, String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.status = status;
        this.codigo = codigo;
    }
}
