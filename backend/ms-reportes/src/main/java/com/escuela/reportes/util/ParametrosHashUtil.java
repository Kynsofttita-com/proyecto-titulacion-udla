package com.escuela.reportes.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
@Component
public class ParametrosHashUtil {

    private static final String ALGORITHM = "SHA-256";

    public String generarHash(Map<String, Object> parametros) {
        try {
            if (parametros == null || parametros.isEmpty()) {
                return generarHashString("");
            }

            TreeMap<String, Object> sorted = new TreeMap<>(parametros);
            String jsonString = convertirAString(sorted);
            return generarHashString(jsonString);
        } catch (NoSuchAlgorithmException ex) {
            log.error("Error generando hash de parámetros: {}", ex.getMessage(), ex);
            throw new RuntimeException("Error en SHA-256: " + ex.getMessage());
        }
    }

    public String generarHash(String... valores) {
        try {
            StringBuilder sb = new StringBuilder();
            for (String valor : valores) {
                if (valor != null) {
                    sb.append(valor).append("|");
                }
            }
            return generarHashString(sb.toString());
        } catch (NoSuchAlgorithmException ex) {
            log.error("Error generando hash: {}", ex.getMessage(), ex);
            throw new RuntimeException("Error en SHA-256: " + ex.getMessage());
        }
    }

    private String generarHashString(String input) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
        byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    private String convertirAString(Map<String, Object> mapa) {
        StringBuilder sb = new StringBuilder();
        mapa.forEach((k, v) -> {
            sb.append(k).append("=");
            if (v != null) {
                sb.append(v.toString());
            }
            sb.append("&");
        });
        return sb.toString();
    }

    public boolean validarHash(Map<String, Object> parametros, String hashEsperado) {
        String hashCalculado = generarHash(parametros);
        return hashCalculado.equals(hashEsperado);
    }
}
