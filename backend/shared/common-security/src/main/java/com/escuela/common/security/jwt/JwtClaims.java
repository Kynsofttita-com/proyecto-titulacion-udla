package com.escuela.common.security.jwt;

/**
 * Constantes de los nombres de claims usados en los JWT del sistema.
 *
 * <p>Centralizar aqui evita typos en strings entre el publisher (MS-Auth)
 * y el consumer (API Gateway).</p>
 */
public final class JwtClaims {

    private JwtClaims() {}

    /** Claim estandar JWT: subject. Valor = userId (Long como String). */
    public static final String SUB = "sub";

    /** Email del usuario autenticado. */
    public static final String EMAIL = "email";

    /** Roles del usuario (lista de strings: "ADMIN", "STAFF", etc.). */
    public static final String ROLES = "roles";

    /** Tipo de token: "ACCESS" o "REFRESH". */
    public static final String TOKEN_TYPE = "type";

    /** Claim estandar JWT: JWT ID (UUID unico, usado para revocacion/rotation). */
    public static final String JTI = "jti";
}
