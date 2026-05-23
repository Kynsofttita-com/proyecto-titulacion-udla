package com.escuela.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuracion de Spring Security para MS-Auth.
 *
 * <p>El JWT se valida en el API Gateway (T4.3). MS-Auth solo necesita:
 * <ul>
 *   <li>{@link PasswordEncoder} con BCrypt para hashear passwords al
 *       crear usuarios y verificar al login.</li>
 *   <li>Una cadena de filtros que permita publicamente todos los endpoints
 *       {@code /auth/**} (login, refresh, etc. — son los que no requieren
 *       autenticacion previa) y los endpoints de actuator/test.</li>
 * </ul>
 *
 * <p>CSRF deshabilitado: la API es stateless (JWT en header/cookie HttpOnly).</p>
 */
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt strength 10 (default Spring) — buen balance seguridad/performance.
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Endpoints TOTALMENTE PUBLICOS (sin validacion alguna)
                        // - POST /auth/login: login sin JWT
                        // - POST /auth/forgot-password: solicitar reset de password
                        // - POST /auth/reset-password: reset con token de recuperacion
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/forgot-password").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/reset-password").permitAll()
                        // Endpoints validados por API Gateway (el Gateway valida JWT y agrega
                        // headers X-User-*). MS-Auth confia en esa validacion: aqui solo dejamos
                        // pasar el request porque la seguridad ya ocurrio en el Gateway.
                        // - POST /auth/refresh: renovar token (publico en Gateway)
                        // - POST /auth/logout: logout (publico en Gateway)
                        // - GET /auth/me: obtener perfil del usuario (protegido en Gateway)
                        .requestMatchers(HttpMethod.POST, "/auth/refresh").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/logout").permitAll()
                        .requestMatchers(HttpMethod.GET, "/auth/me").permitAll()
                        // Actuator y Swagger
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // Endpoints de dominio: el Gateway valida JWT y agrega headers X-User-*.
                        // MS-Auth confia en esa validacion y aplica autorizacion via AuthHeaderGuard
                        // en cada controller (requireAuth + requireAnyRole).
                        .anyRequest().permitAll()
                )
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable());

        return http.build();
    }
}
