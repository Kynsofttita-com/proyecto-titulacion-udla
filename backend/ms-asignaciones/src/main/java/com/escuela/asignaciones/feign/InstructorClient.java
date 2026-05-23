package com.escuela.asignaciones.feign;

import com.escuela.asignaciones.dto.feign.InstructorDetailDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ms-instructores", url = "${FEIGN_MS_INSTRUCTORES_URL:http://localhost:8083}")
public interface InstructorClient {

    @CircuitBreaker(name = "instructorClientCB", fallbackMethod = "obtenerInstructorFallback")
    @GetMapping("/instructores/{id}")
    InstructorDetailDTO obtenerInstructor(@PathVariable("id") Long id);

    default InstructorDetailDTO obtenerInstructorFallback(Long id, Exception ex) {
        return new InstructorDetailDTO(id, "INACTIVO");
    }
}
