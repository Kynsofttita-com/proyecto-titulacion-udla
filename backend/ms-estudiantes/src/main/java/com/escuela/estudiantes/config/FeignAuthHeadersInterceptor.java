package com.escuela.estudiantes.config;

import com.escuela.common.security.headers.UserHeaders;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Propaga los headers de autenticacion ({@code X-User-Email},
 * {@code X-User-Roles}) del request HTTP actual a las llamadas Feign salientes.
 *
 * <p>Necesario porque los microservicios downstream (p.ej. MS-Auth) validan
 * permisos usando esos headers que el API Gateway inserta tras validar el
 * JWT. Si Feign no los reenvia, el MS destino responde 401/403.</p>
 *
 * <p>Si no hay request en contexto (p.ej. listener RabbitMQ o tarea
 * programada), no propaga nada -- el llamado fallara con 401, que es lo
 * esperado.</p>
 */
@Configuration
public class FeignAuthHeadersInterceptor {

    private static final Logger log = LoggerFactory.getLogger(FeignAuthHeadersInterceptor.class);

    @org.springframework.context.annotation.Bean
    public RequestInterceptor userHeadersInterceptor() {
        return (RequestTemplate template) -> {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) {
                log.debug("Sin request HTTP en contexto; no se propagan headers a {}", template.url());
                return;
            }
            HttpServletRequest req = attrs.getRequest();
            String email = req.getHeader(UserHeaders.USER_EMAIL);
            String roles = req.getHeader(UserHeaders.USER_ROLES);
            if (email != null) template.header(UserHeaders.USER_EMAIL, email);
            if (roles != null) template.header(UserHeaders.USER_ROLES, roles);
        };
    }
}
