package com.escuela.estudiantes.feign;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * Cliente Feign hacia ms-auth (que aloja el catalogo) para obtener un
 * tipo de curso por id. Lo usamos en ProgresoAcademicoService para conocer
 * la duracion total del curso del estudiante y calcular el % de avance.
 *
 * <p>Usamos {@code Map<String,Object>} para no acoplarnos al DTO interno
 * de ms-auth. Las claves relevantes que consumimos: {@code id, nombre,
 * duracionTotalHoras, precioBase, categoriaLicenciaId}.</p>
 */
@FeignClient(name = "ms-auth-tipos-curso", contextId = "tipoCursoClient",
        url = "${FEIGN_MS_AUTH_URL:http://ms-auth:8081}", path = "/tipos-curso")
public interface TipoCursoClient {

    @CircuitBreaker(name = "tipoCursoClientCB", fallbackMethod = "obtenerFallback")
    @GetMapping("/{id}")
    Map<String, Object> obtener(@PathVariable("id") Long id);

    /** Fallback: si ms-auth no responde, devolvemos un map vacio para no romper el calculo del % */
    default Map<String, Object> obtenerFallback(Long id, Exception ex) {
        return Map.of();
    }
}
