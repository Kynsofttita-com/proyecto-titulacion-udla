package com.escuela.cobros.client;

import com.escuela.cobros.dto.EstudianteDetailDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
    name = "ms-estudiantes",
    url = "${FEIGN_MS_ESTUDIANTES_URL:http://localhost:8082}"
)
public interface EstudianteClient {

    @CircuitBreaker(name = "estudianteClientCB", fallbackMethod = "obtenerEstianteFallback")
    @GetMapping("/estudiantes/{id}")
    EstudianteDetailDTO obtenerEstudiante(@PathVariable Long id);

    default EstudianteDetailDTO obtenerEstianteFallback(Long id, Exception ex) {
        return new EstudianteDetailDTO(id, "INACTIVO");
    }
}
