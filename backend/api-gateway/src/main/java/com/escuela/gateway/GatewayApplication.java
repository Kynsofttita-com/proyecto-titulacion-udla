package com.escuela.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Punto de entrada del API Gateway.
 *
 * <p>Es el único endpoint que el frontend conoce. Todas las peticiones HTTP
 * pasan por aquí y son ruteadas al microservicio correspondiente.</p>
 *
 * <p>Puerto: <b>8080</b></p>
 *
 * <p>A partir del Sprint 4, este Gateway también validará JWT, propagará claims
 * a los microservicios via headers (X-User-Id, X-User-Roles) y aplicará rate limiting.</p>
 */
@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
