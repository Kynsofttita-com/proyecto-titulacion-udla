package com.escuela.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * MS-Auth - Microservicio de Autenticación, Autorización y Configuración.
 *
 * <p>Responsabilidades:</p>
 * <ul>
 *   <li>Login/logout y generación de JWT (a partir de Sprint 4)</li>
 *   <li>Gestión de usuarios, roles y permisos (Sprint 4)</li>
 *   <li>Recuperación de contraseña con email (Sprint 4)</li>
 *   <li>Account lockout tras 3 intentos fallidos (Sprint 4)</li>
 *   <li>Módulo de Configuración del sistema (single-tenant)</li>
 *   <li>Audit log centralizado (consume eventos de los demás MS)</li>
 * </ul>
 *
 * <p>Puerto: <b>8081</b> &nbsp;|&nbsp; Schema BD: <b>auth_schema</b></p>
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableJpaAuditing
@EnableCaching
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
