package com.escuela.instructores;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * ms-instructores - Microservicio de gestión de instructores.
 *
 * <p>Puerto: <b>8083</b> &nbsp;|&nbsp; Schema BD: <b>instructores_schema</b></p>
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableJpaAuditing
@EnableCaching
public class InstructoresApplication {

    public static void main(String[] args) {
        SpringApplication.run(InstructoresApplication.class, args);
    }
}
