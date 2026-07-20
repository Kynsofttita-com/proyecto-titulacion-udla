package com.escuela.instructores.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Propaga los headers de identidad (X-User-Id, X-User-Email, X-User-Roles)
 * en toda llamada Feign hacia otros microservicios. Sin esto, los MS
 * destino responden 401 porque no pueden identificar al usuario.
 *
 * Para llamadas async/scheduler/eventos (sin request HTTP padre) se usan
 * defaults de servicio (SYSTEM,ADMIN) equivalentes a los que ya usa
 * ms-asignaciones y ms-cobros.
 */
@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor authHeaderPropagator() {
        return template -> {
            String userId = null;
            String userEmail = null;
            String userRoles = null;
            try {
                ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attrs != null) {
                    HttpServletRequest req = attrs.getRequest();
                    userId = req.getHeader("X-User-Id");
                    userEmail = req.getHeader("X-User-Email");
                    userRoles = req.getHeader("X-User-Roles");
                }
            } catch (Exception ignored) {
            }
            if (userEmail == null) {
                userId = "0";
                userEmail = "system@escuela.local";
                userRoles = "SYSTEM,ADMIN";
            }
            template.header("X-User-Id", userId);
            template.header("X-User-Email", userEmail);
            template.header("X-User-Roles", userRoles);
        };
    }
}
