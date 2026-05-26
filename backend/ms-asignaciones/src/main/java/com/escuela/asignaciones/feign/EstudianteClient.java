package com.escuela.asignaciones.feign;

import com.escuela.asignaciones.dto.feign.EstudianteDetailDTO;
import com.escuela.asignaciones.dto.feign.IncrementarHorasFeignRequest;
import com.escuela.asignaciones.dto.feign.IncrementarHorasFeignResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ms-estudiantes", url = "${FEIGN_MS_ESTUDIANTES_URL:http://localhost:8082}")
public interface EstudianteClient {

    @CircuitBreaker(name = "estudianteClientCB", fallbackMethod = "obtenerEstianteFallback")
    @GetMapping("/estudiantes/{id}")
    EstudianteDetailDTO obtenerEstudiante(@PathVariable("id") Long id);

    default EstudianteDetailDTO obtenerEstianteFallback(Long id, Exception ex) {
        return new EstudianteDetailDTO(id, "INACTIVO", null, null);
    }

    /**
     * Suma minutos al acumulado del estudiante. Lo invocamos al finalizar una clase
     * para que el estudiante avance MATRICULADO -> CURSANDO automáticamente.
     */
    @CircuitBreaker(name = "estudianteClientCB", fallbackMethod = "incrementarHorasFallback")
    @PutMapping("/estudiantes/{id}/horas-completadas/incrementar")
    IncrementarHorasFeignResponse incrementarHoras(
            @PathVariable("id") Long id,
            @RequestBody IncrementarHorasFeignRequest request);

    default IncrementarHorasFeignResponse incrementarHorasFallback(
            Long id, IncrementarHorasFeignRequest request, Exception ex) {
        return new IncrementarHorasFeignResponse(id, null, null, null, false,
                "ms-estudiantes no respondió: " + ex.getMessage());
    }
}
