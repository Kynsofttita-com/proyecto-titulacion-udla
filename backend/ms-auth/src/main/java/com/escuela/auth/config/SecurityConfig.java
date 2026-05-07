package com.escuela.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
                        // Endpoints publicos (no requieren autenticacion)
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        // Endpoint de prueba T3.3 (se elimina en T4.5)
                        .requestMatchers("/test/**").permitAll()
                        // Swagger UI
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // Resto requiere autenticacion (vendra cuando MS-Auth tenga endpoints administrativos en Sprint 5+)
                        .anyRequest().authenticated()
                )
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable());

        return http.build();
    }
}
