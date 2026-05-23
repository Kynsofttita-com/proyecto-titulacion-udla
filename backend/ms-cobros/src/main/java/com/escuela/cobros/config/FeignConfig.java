package com.escuela.cobros.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

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
