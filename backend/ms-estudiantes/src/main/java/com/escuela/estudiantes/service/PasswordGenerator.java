package com.escuela.estudiantes.service;

import java.security.SecureRandom;

/**
 * Genera contrasenas temporales que cumplen el regex de validacion de MS-Auth:
 * {@code ^(?=.*[A-Z])(?=.*[a-z])(?=.*\d).+$} y minimo 8 caracteres.
 *
 * <p>La password generada se entrega a MS-Auth con
 * {@code passwordChangeRequired = true} para que el estudiante deba cambiarla
 * en su primer login.</p>
 */
final class PasswordGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String MAYUSCULAS = "ABCDEFGHJKLMNPQRSTUVWXYZ";   // sin I, O
    private static final String MINUSCULAS = "abcdefghijkmnopqrstuvwxyz";  // sin l
    private static final String DIGITOS    = "23456789";                   // sin 0, 1
    private static final String TODOS = MAYUSCULAS + MINUSCULAS + DIGITOS;

    private PasswordGenerator() {}

    /**
     * Genera una password de 12 caracteres con al menos 1 mayuscula, 1
     * minuscula y 1 digito. El orden de caracteres se mezcla.
     */
    static String generarTemporal() {
        StringBuilder sb = new StringBuilder(12);
        sb.append(MAYUSCULAS.charAt(RANDOM.nextInt(MAYUSCULAS.length())));
        sb.append(MINUSCULAS.charAt(RANDOM.nextInt(MINUSCULAS.length())));
        sb.append(DIGITOS.charAt(RANDOM.nextInt(DIGITOS.length())));
        for (int i = 0; i < 9; i++) {
            sb.append(TODOS.charAt(RANDOM.nextInt(TODOS.length())));
        }
        // Mezcla Fisher-Yates para que los 3 primeros no esten siempre fijos
        char[] chars = sb.toString().toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            char tmp = chars[i];
            chars[i] = chars[j];
            chars[j] = tmp;
        }
        return new String(chars);
    }
}
