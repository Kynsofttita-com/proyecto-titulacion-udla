package com.escuela.common.validation.core;

import java.util.regex.Pattern;

/**
 * Validador de telefonos ecuatorianos (DECISIONES.md seccion 11).
 *
 * <p>Formatos aceptados:</p>
 * <ul>
 *   <li><b>Movil:</b> {@code 09XXXXXXXX} (10 digitos iniciando con 09).</li>
 *   <li><b>Fijo Quito:</b> {@code 02XXXXXXX} (9 digitos iniciando con 02).</li>
 *   <li><b>Fijo otras provincias:</b> {@code 0[3-7]XXXXXXX} (9 digitos
 *       iniciando con 03, 04, 05, 06 o 07).</li>
 * </ul>
 *
 * <p>No se acepta prefijo internacional (+593) en este validador; si se
 * requiere, normalizar a formato local antes de validar.</p>
 */
public final class TelefonoEcuadorValidator {

    private static final Pattern MOVIL = Pattern.compile("^09\\d{8}$");
    private static final Pattern FIJO = Pattern.compile("^0[2-7]\\d{7}$");

    private TelefonoEcuadorValidator() {
        // utility class
    }

    public static boolean isValid(String telefono) {
        if (telefono == null || telefono.isBlank()) {
            return false;
        }
        return MOVIL.matcher(telefono).matches() || FIJO.matcher(telefono).matches();
    }

    public static boolean esMovil(String telefono) {
        return telefono != null && MOVIL.matcher(telefono).matches();
    }

    public static boolean esFijo(String telefono) {
        return telefono != null && FIJO.matcher(telefono).matches();
    }
}
