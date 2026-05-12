package com.escuela.estudiantes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * ms-estudiantes - Microservicio de gestión de estudiantes.
 *
 * <p>Puerto: <b>8082</b> &nbsp;|&nbsp; Schema BD: <b>estudiantes_schema</b></p>
 *
 * <p>JPA Auditing se activa en {@link com.escuela.estudiantes.config.JpaAuditingActivationConfig}
 * (separado para que @WebMvcTest no falle al cargar el contexto).</p>
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableCaching
public class EstudiantesApplication {

    public static void main(String[] args) {
        SpringApplication.run(EstudiantesApplication.class, args);
    }
}
