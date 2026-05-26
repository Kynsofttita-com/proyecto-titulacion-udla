package com.escuela.asignaciones.feign;

import com.escuela.asignaciones.dto.feign.DisponibilidadDelDiaDTO;
import com.escuela.asignaciones.dto.feign.InstructorDetailDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.Collections;

@FeignClient(name = "ms-instructores", url = "${FEIGN_MS_INSTRUCTORES_URL:http://localhost:8083}")
public interface InstructorClient {

    @CircuitBreaker(name = "instructorClientCB", fallbackMethod = "obtenerInstructorFallback")
    @GetMapping("/instructores/{id}")
    InstructorDetailDTO obtenerInstructor(@PathVariable("id") Long id);

    default InstructorDetailDTO obtenerInstructorFallback(Long id, Exception ex) {
        return new InstructorDetailDTO(id, "INACTIVO");
    }

    /**
     * Consulta la disponibilidad real del instructor en una fecha específica:
     * combina su horario semanal recurrente con las excepciones (AUSENCIA/EXTRA).
     */
    @CircuitBreaker(name = "instructorClientCB", fallbackMethod = "disponibilidadFallback")
    @GetMapping("/instructores/{id}/disponibilidad")
    DisponibilidadDelDiaDTO obtenerDisponibilidad(
            @PathVariable("id") Long id,
            @RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha);

    /**
     * Fallback: asume NO disponible para evitar agendar clases si ms-instructores no responde.
     * Es más seguro rechazar que crear una clase potencialmente inválida.
     */
    default DisponibilidadDelDiaDTO disponibilidadFallback(Long id, LocalDate fecha, Exception ex) {
        return new DisponibilidadDelDiaDTO(
                id, fecha.toString(), (short) fecha.getDayOfWeek().getValue(),
                false, "ms-instructores no respondió: " + ex.getMessage(),
                Collections.emptyList()
        );
    }
}
