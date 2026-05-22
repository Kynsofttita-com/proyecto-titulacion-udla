package com.escuela.common.validation.core;

import java.util.regex.Pattern;

/**
 * Validador de placa vehicular ecuatoriana (DECISIONES.md seccion 11).
 *
 * <p>Formatos aceptados:</p>
 * <ul>
 *   <li><b>Particular:</b> {@code ABC-1234} (3 letras + guion + 4 digitos).</li>
 *   <li><b>Comercial / institucional:</b> {@code AB-1234A}
 *       (2 letras + guion + 4 digitos + 1 letra).</li>
 * </ul>
 *
 * <p>Letras siempre en mayuscula. La primera letra debe corresponder a una
 * provincia (A-Z excluyendo letras no asignadas como F, Q, V, etc.), pero
 * por simplicidad este validador acepta cualquier letra A-Z; la verificacion
 * estricta por provincia se delega a una capa de auditoria si es necesaria.</p>
 */
public final class PlacaEcuadorValidator {

    private static final Pattern PARTICULAR = Pattern.compile("^[A-Z]{3}-\\d{4}$");
    private static final Pattern COMERCIAL = Pattern.compile("^[A-Z]{2}-\\d{4}[A-Z]$");

    private PlacaEcuadorValidator() {
        // utility class
    }

    public static boolean isValid(String placa) {
        if (placa == null || placa.isBlank()) {
            return false;
        }
        return PARTICULAR.matcher(placa).matches() || COMERCIAL.matcher(placa).matches();
    }
}
