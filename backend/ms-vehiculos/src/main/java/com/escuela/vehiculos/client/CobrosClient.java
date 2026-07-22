package com.escuela.vehiculos.client;

import com.escuela.vehiculos.dto.MovimientoVehiculoRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Cliente Feign hacia ms-cobros. Sincroniza los gastos contables originados
 * en Vehiculos (combustible + mantenimiento).
 *
 * <p>Todos los metodos son best-effort desde el lado de negocio: si la
 * llamada falla, el service loguea warning pero NO tumba la operacion de
 * Vehiculos (el registro se guarda igual y la Tarea 5 backfill los recupera).</p>
 */
@FeignClient(
        name = "ms-cobros",
        url = "${FEIGN_MS_COBROS_URL:http://localhost:8086}"
)
public interface CobrosClient {

    // -------- combustible --------

    @CircuitBreaker(name = "cobrosClientCB", fallbackMethod = "crearCombustibleFallback")
    @PostMapping("/internal/movimientos-vehiculo/combustible/{registroId}")
    void crearMovimientoCombustible(@PathVariable("registroId") Long registroId,
                                    @RequestBody MovimientoVehiculoRequest request);

    default void crearCombustibleFallback(Long registroId, MovimientoVehiculoRequest request, Throwable ex) {
        // Fallback: no-op. El service llamador loguea el warning.
    }

    @CircuitBreaker(name = "cobrosClientCB", fallbackMethod = "actualizarCombustibleFallback")
    @PutMapping("/internal/movimientos-vehiculo/combustible/{registroId}")
    void actualizarMovimientoCombustible(@PathVariable("registroId") Long registroId,
                                         @RequestBody MovimientoVehiculoRequest request);

    default void actualizarCombustibleFallback(Long registroId, MovimientoVehiculoRequest request, Throwable ex) {
    }

    @CircuitBreaker(name = "cobrosClientCB", fallbackMethod = "anularCombustibleFallback")
    @DeleteMapping("/internal/movimientos-vehiculo/combustible/{registroId}")
    void anularMovimientoCombustible(@PathVariable("registroId") Long registroId,
                                     @RequestParam(value = "motivo", required = false) String motivo);

    default void anularCombustibleFallback(Long registroId, String motivo, Throwable ex) {
    }

    // -------- mantenimiento --------

    @CircuitBreaker(name = "cobrosClientCB", fallbackMethod = "crearMantenimientoFallback")
    @PostMapping("/internal/movimientos-vehiculo/mantenimiento/{mantenimientoId}")
    void crearMovimientoMantenimiento(@PathVariable("mantenimientoId") Long mantenimientoId,
                                      @RequestBody MovimientoVehiculoRequest request);

    default void crearMantenimientoFallback(Long mantenimientoId, MovimientoVehiculoRequest request, Throwable ex) {
    }

    @CircuitBreaker(name = "cobrosClientCB", fallbackMethod = "actualizarMantenimientoFallback")
    @PutMapping("/internal/movimientos-vehiculo/mantenimiento/{mantenimientoId}")
    void actualizarMovimientoMantenimiento(@PathVariable("mantenimientoId") Long mantenimientoId,
                                           @RequestBody MovimientoVehiculoRequest request);

    default void actualizarMantenimientoFallback(Long mantenimientoId, MovimientoVehiculoRequest request, Throwable ex) {
    }

    @CircuitBreaker(name = "cobrosClientCB", fallbackMethod = "anularMantenimientoFallback")
    @DeleteMapping("/internal/movimientos-vehiculo/mantenimiento/{mantenimientoId}")
    void anularMovimientoMantenimiento(@PathVariable("mantenimientoId") Long mantenimientoId,
                                       @RequestParam(value = "motivo", required = false) String motivo);

    default void anularMantenimientoFallback(Long mantenimientoId, String motivo, Throwable ex) {
    }
}
