package com.escuela.asignaciones;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * ms-asignaciones - Microservicio de programación de clases (tripartita).
 *
 * <p>Puerto: <b>8085</b> &nbsp;|&nbsp; Schema BD: <b>asignaciones_schema</b></p>
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableJpaAuditing
@EnableCaching
public class AsignacionesApplication {

    public static void main(String[] args) {
        SpringApplication.run(AsignacionesApplication.class, args);
    }
}
