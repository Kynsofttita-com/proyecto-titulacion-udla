package com.escuela.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Punto de entrada del Eureka Server (service discovery del sistema).
 *
 * <p>Todos los microservicios se registran en este servidor al iniciar
 * y consultan aquí las direcciones de los demás servicios.</p>
 *
 * <p>Dashboard disponible en: <a href="http://localhost:8761">http://localhost:8761</a></p>
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
