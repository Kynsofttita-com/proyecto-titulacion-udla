package com.escuela.asignaciones.feign;

import com.escuela.asignaciones.dto.feign.VehiculoDetailDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ms-vehiculos", url = "${FEIGN_MS_VEHICULOS_URL:http://localhost:8084}")
public interface VehiculoClient {

    @CircuitBreaker(name = "vehiculoClientCB", fallbackMethod = "obtenerVehiculoFallback")
    @GetMapping("/vehiculos/{id}")
    VehiculoDetailDTO obtenerVehiculo(@PathVariable("id") Long id);

    default VehiculoDetailDTO obtenerVehiculoFallback(Long id, Exception ex) {
        return new VehiculoDetailDTO(id, null);
    }
}
