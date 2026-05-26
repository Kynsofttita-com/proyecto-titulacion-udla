package com.escuela.asignaciones.feign;

import com.escuela.asignaciones.dto.feign.ActualizarKilometrajeFeignRequest;
import com.escuela.asignaciones.dto.feign.ActualizarKilometrajeFeignResponse;
import com.escuela.asignaciones.dto.feign.VehiculoDetailDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ms-vehiculos", url = "${FEIGN_MS_VEHICULOS_URL:http://localhost:8084}")
public interface VehiculoClient {

    @CircuitBreaker(name = "vehiculoClientCB", fallbackMethod = "obtenerVehiculoFallback")
    @GetMapping("/vehiculos/{id}")
    VehiculoDetailDTO obtenerVehiculo(@PathVariable("id") Long id);

    default VehiculoDetailDTO obtenerVehiculoFallback(Long id, Exception ex) {
        return new VehiculoDetailDTO(id, null, null, null, null, null, null, null);
    }

    /**
     * Sincroniza el odómetro maestro del vehículo al finalizar una clase.
     * Es monotónico en el destino: si el km enviado <= km actual, no se aplica.
     */
    @CircuitBreaker(name = "vehiculoClientCB", fallbackMethod = "actualizarKilometrajeFallback")
    @PutMapping("/vehiculos/{id}/kilometraje")
    ActualizarKilometrajeFeignResponse actualizarKilometraje(
            @PathVariable("id") Long id,
            @RequestBody ActualizarKilometrajeFeignRequest request);

    default ActualizarKilometrajeFeignResponse actualizarKilometrajeFallback(
            Long id, ActualizarKilometrajeFeignRequest request, Exception ex) {
        return new ActualizarKilometrajeFeignResponse(id, null, null, false,
                "ms-vehiculos no respondio: " + ex.getMessage());
    }
}
