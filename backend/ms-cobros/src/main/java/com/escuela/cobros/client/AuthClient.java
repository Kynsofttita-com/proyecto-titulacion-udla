package com.escuela.cobros.client;

import com.escuela.cobros.dto.TipoCursoDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Cliente Feign hacia MS-Auth para consultar el catalogo de tipos de curso.
 * MS-Cobros necesita el precio base del curso para calcular saldo total del
 * estudiante al emitir o consultar facturas.
 */
@FeignClient(
    name = "ms-auth",
    url = "${FEIGN_MS_AUTH_URL:http://localhost:8081}"
)
public interface AuthClient {

    @CircuitBreaker(name = "authClientCB", fallbackMethod = "obtenerTipoCursoFallback")
    @GetMapping("/tipos-curso/{id}")
    TipoCursoDTO obtenerTipoCurso(@PathVariable("id") Long id);

    default TipoCursoDTO obtenerTipoCursoFallback(Long id, Throwable ex) {
        // Fallback minimal cuando ms-auth no responde: precio 0 → la UI lo
        // mostrara como "Sin curso asignado" y bloqueara la creacion.
        return new TipoCursoDTO(id, null, null, null, null, null, null);
    }
}
