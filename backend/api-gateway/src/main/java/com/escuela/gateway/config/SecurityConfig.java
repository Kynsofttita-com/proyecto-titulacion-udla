package com.escuela.gateway.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Configuracion de Spring Security para el API Gateway (reactive / Webflux).
 *
 * <p>Deshabilita la auth por defecto de Spring Security para que sea
 * {@link com.escuela.gateway.filter.JwtAuthenticationGlobalFilter} quien
 * controle la autorizacion JWT. Esto da control total al filtro y evita
 * conflictos con la cadena de filtros default de Spring.</p>
 */
@Configuration
@EnableWebFluxSecurity
@EnableConfigurationProperties(GatewayProperties.class)
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(csrf -> csrf.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable())
                // Permitir todo a nivel de Spring Security; el control real
                // lo hace el JwtAuthenticationGlobalFilter (Spring Cloud Gateway).
                .authorizeExchange(auth -> auth.anyExchange().permitAll())
                .build();
    }
}
