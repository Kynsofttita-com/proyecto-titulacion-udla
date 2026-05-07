package com.escuela.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Propiedades del Gateway. Mapea {@code escuela.gateway.*} de application.yml.
 */
@Data
@ConfigurationProperties(prefix = "escuela.gateway")
public class GatewayProperties {

    /**
     * Lista de paths que NO requieren JWT (endpoints publicos).
     * Soporta Ant patterns: {@code /auth/**}, {@code /actuator/**}, etc.
     */
    private List<String> publicPaths = new ArrayList<>();
}
