package com.escuela.cobros.client;

import com.escuela.cobros.dto.ConfiguracionEscuelaDTO;
import com.escuela.cobros.dto.TipoCursoDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Cliente Feign hacia MS-Auth. Se usa para:
 * <ul>
 *   <li>Consultar el catalogo de tipos de curso (precio base para facturas).</li>
 *   <li>Leer la configuracion de la escuela (cuentas contables por defecto).</li>
 * </ul>
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

    /**
     * Trae la configuracion de la escuela (single-tenant). Devuelve solo los
     * campos que ms-cobros necesita hoy (cuentas por defecto). El resto del
     * payload se ignora gracias a {@code FAIL_ON_UNKNOWN_PROPERTIES=false}
     * (default de Spring Boot).
     */
    @CircuitBreaker(name = "authClientCB", fallbackMethod = "obtenerConfiguracionFallback")
    @GetMapping("/configuracion")
    ConfiguracionEscuelaDTO obtenerConfiguracion();

    default ConfiguracionEscuelaDTO obtenerConfiguracionFallback(Throwable ex) {
        // Sin config → no hay defaults; el caller debe manejar el caso.
        return new ConfiguracionEscuelaDTO(null, null, null);
    }
}
