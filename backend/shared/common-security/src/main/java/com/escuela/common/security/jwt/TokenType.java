package com.escuela.common.security.jwt;

/**
 * Tipos de token JWT soportados.
 *
 * <p><b>ACCESS:</b> token de corta duracion (15 min) que viaja en cada request
 * para autenticar al usuario. Si filtra, la ventana de exposicion es minima.</p>
 *
 * <p><b>REFRESH:</b> token de larga duracion (7 dias) usado UNICAMENTE para
 * obtener nuevos access tokens vis {@code POST /auth/refresh}. Cada uso
 * genera uno nuevo (rotation) y el anterior se invalida.</p>
 */
public enum TokenType {
    ACCESS,
    REFRESH
}
