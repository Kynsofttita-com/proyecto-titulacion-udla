package com.escuela.estudiantes.service;

/**
 * Validador del algoritmo del digito verificador de la cedula ecuatoriana.
 *
 * <p>Reglas:</p>
 * <ol>
 *   <li>Exactamente 10 digitos numericos.</li>
 *   <li>Los primeros 2 digitos (codigo de provincia) entre 01 y 24.</li>
 *   <li>El tercer digito menor a 6 (persona natural).</li>
 *   <li>Los digitos 1,3,5,7,9 se multiplican por 2 (si pasa de 9 se resta 9).</li>
 *   <li>Los digitos 2,4,6,8 quedan tal cual.</li>
 *   <li>Se suman los 9 primeros valores; el digito 10 debe ser la diferencia
 *       entre la decena inmediatamente superior y esa suma (o 0 si la suma
 *       es multiplo de 10).</li>
 * </ol>
 *
 * <p>El formato (10 digitos) se valida antes con {@code @Pattern} en el DTO;
 * esta clase valida solo el digito verificador.</p>
 */
public final class CedulaValidator {

    private CedulaValidator() {
        // utility class
    }

    public static boolean isValid(String cedula) {
        if (cedula == null || cedula.length() != 10) {
            return false;
        }
        for (int i = 0; i < 10; i++) {
            if (!Character.isDigit(cedula.charAt(i))) {
                return false;
            }
        }

        int provincia = Integer.parseInt(cedula.substring(0, 2));
        if (provincia < 1 || provincia > 24) {
            return false;
        }

        int tercerDigito = Character.getNumericValue(cedula.charAt(2));
        if (tercerDigito >= 6) {
            return false;
        }

        int suma = 0;
        for (int i = 0; i < 9; i++) {
            int digito = Character.getNumericValue(cedula.charAt(i));
            if (i % 2 == 0) {
                // posiciones impares (1, 3, 5, 7, 9) = indices 0,2,4,6,8
                int doble = digito * 2;
                suma += (doble > 9) ? doble - 9 : doble;
            } else {
                suma += digito;
            }
        }

        int decenaSuperior = ((suma + 9) / 10) * 10;
        int verificadorEsperado = decenaSuperior - suma;
        if (verificadorEsperado == 10) {
            verificadorEsperado = 0;
        }

        int verificadorReal = Character.getNumericValue(cedula.charAt(9));
        return verificadorEsperado == verificadorReal;
    }
}
