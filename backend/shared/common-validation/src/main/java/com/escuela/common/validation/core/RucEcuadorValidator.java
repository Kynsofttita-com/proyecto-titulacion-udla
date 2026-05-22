package com.escuela.common.validation.core;

/**
 * Validador del RUC ecuatoriano (13 digitos).
 *
 * <p>Tipos soportados (DECISIONES.md seccion 11):</p>
 * <ul>
 *   <li><b>Persona natural:</b> 10 primeros digitos = cedula valida,
 *       sufijo "001" (RUC = cedula + "001"). Tercer digito menor a 6.</li>
 *   <li><b>Sociedad privada / extranjero sin cedula:</b> tercer digito
 *       igual a 9, sufijo "001". Valida modulo 11 sobre los primeros 9
 *       con coeficientes (4,3,2,7,6,5,4,3,2).</li>
 *   <li><b>Sociedad publica:</b> tercer digito igual a 6, los primeros 8
 *       digitos forman el numero base, sufijo "0001". Valida modulo 11
 *       con coeficientes (3,2,7,6,5,4,3,2).</li>
 * </ul>
 */
public final class RucEcuadorValidator {

    private RucEcuadorValidator() {
        // utility class
    }

    public static boolean isValid(String ruc) {
        if (ruc == null || ruc.length() != 13) {
            return false;
        }
        for (int i = 0; i < 13; i++) {
            if (!Character.isDigit(ruc.charAt(i))) {
                return false;
            }
        }

        int provincia = Integer.parseInt(ruc.substring(0, 2));
        if (provincia < 1 || provincia > 24) {
            return false;
        }

        int tercerDigito = Character.getNumericValue(ruc.charAt(2));

        if (tercerDigito < 6) {
            // Persona natural: primeros 10 = cedula valida + "001"
            if (!"001".equals(ruc.substring(10, 13))) {
                return false;
            }
            return CedulaEcuadorValidator.isValid(ruc.substring(0, 10));
        } else if (tercerDigito == 6) {
            // Sociedad publica: digitos 1-8 + verificador en 9 + "0001"
            if (!"0001".equals(ruc.substring(9, 13))) {
                return false;
            }
            int[] coeficientes = {3, 2, 7, 6, 5, 4, 3, 2};
            return verificarModulo11(ruc, coeficientes, 8);
        } else if (tercerDigito == 9) {
            // Sociedad privada o extranjero: digitos 1-9 + verificador en 10 + "001"
            if (!"001".equals(ruc.substring(10, 13))) {
                return false;
            }
            int[] coeficientes = {4, 3, 2, 7, 6, 5, 4, 3, 2};
            return verificarModulo11(ruc, coeficientes, 9);
        }
        return false;
    }

    private static boolean verificarModulo11(String ruc, int[] coeficientes, int len) {
        int suma = 0;
        for (int i = 0; i < len; i++) {
            suma += Character.getNumericValue(ruc.charAt(i)) * coeficientes[i];
        }
        int residuo = suma % 11;
        int verificadorEsperado = (residuo == 0) ? 0 : 11 - residuo;
        if (verificadorEsperado == 10) {
            return false;
        }
        int verificadorReal = Character.getNumericValue(ruc.charAt(len));
        return verificadorEsperado == verificadorReal;
    }
}
