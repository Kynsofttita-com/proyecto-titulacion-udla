package com.escuela.common.security.headers;

/**
 * Constantes de los nombres de headers HTTP que el API Gateway agrega a cada
 * request despues de validar el JWT, y que los microservicios downstream leen
 * para identificar al usuario autenticado.
 *
 * <p>Convencion: {@code X-User-*} para datos del usuario.</p>
 */
public final class UserHeaders {

    private UserHeaders() {}

    public static final String USER_ID    = "X-User-Id";
    public static final String USER_EMAIL = "X-User-Email";
    /** Lista de roles separados por coma. Ej: "ADMIN,STAFF". */
    public static final String USER_ROLES = "X-User-Roles";
    /** Correlation ID para trazabilidad cross-MS. */
    public static final String CORRELATION_ID = "X-Correlation-Id";
}
