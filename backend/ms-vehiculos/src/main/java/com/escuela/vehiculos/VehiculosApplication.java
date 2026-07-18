package com.escuela.vehiculos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * ms-vehiculos - Microservicio de gestión de flota vehicular.
 *
 * <p>Puerto: <b>8084</b> &nbsp;|&nbsp; Schema BD: <b>vehiculos_schema</b></p>
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@EnableCaching
@EnableScheduling
public class VehiculosApplication {

    public static void main(String[] args) {
        SpringApplication.run(VehiculosApplication.class, args);
    }
}
