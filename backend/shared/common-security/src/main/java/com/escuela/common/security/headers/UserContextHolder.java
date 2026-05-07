package com.escuela.common.security.headers;

/**
 * Holder thread-local del {@link UserContext} actual.
 *
 * <p>Cada microservicio downstream tiene un filtro (Sprint 4 / T4.x) que lee
 * los headers {@code X-User-*} al inicio del request y popula este holder.
 * Al terminar el request (o en caso de excepcion), el filtro debe llamar
 * {@link #clear()} para evitar leaks entre requests reusando el thread.</p>
 *
 * <p>Uso desde un service o controller:</p>
 * <pre>
 * UserContext user = UserContextHolder.requireContext();
 * if (!user.hasRole("ADMIN")) {
 *     throw new ForbiddenException();
 * }
 * </pre>
 */
public final class UserContextHolder {

    private static final ThreadLocal<UserContext> CONTEXT = new ThreadLocal<>();

    private UserContextHolder() {}

    /** Establece el contexto del usuario para el thread actual. */
    public static void setContext(UserContext context) {
        CONTEXT.set(context);
    }

    /** Devuelve el contexto del usuario actual o {@code null} si no esta autenticado. */
    public static UserContext getContext() {
        return CONTEXT.get();
    }

    /**
     * Devuelve el contexto del usuario actual o lanza {@link IllegalStateException}
     * si no hay usuario autenticado. Usar cuando se sabe que el endpoint requiere autenticacion.
     */
    public static UserContext requireContext() {
        UserContext ctx = CONTEXT.get();
        if (ctx == null) {
            throw new IllegalStateException(
                    "No hay UserContext en el thread actual. " +
                    "Verificar que el endpoint este protegido y que el filtro este configurado.");
        }
        return ctx;
    }

    /** Limpia el ThreadLocal. <b>OBLIGATORIO</b> al final de cada request. */
    public static void clear() {
        CONTEXT.remove();
    }
}
