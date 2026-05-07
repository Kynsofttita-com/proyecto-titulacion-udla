package com.escuela.cobros;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * ms-cobros - Microservicio de facturación y pagos.
 *
 * <p>Puerto: <b>8086</b> &nbsp;|&nbsp; Schema BD: <b>cobros_schema</b></p>
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@EnableCaching
public class CobrosApplication {

    public static void main(String[] args) {
        SpringApplication.run(CobrosApplication.class, args);
    }
}
