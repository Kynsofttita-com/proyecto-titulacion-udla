package com.escuela.estudiantes.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * Cliente Feign hacia MS-Cobros para consultar la situacion_pago calculada
 * en vivo a partir de facturas y cuotas. Usado solo para el endpoint admin
 * de sincronizacion masiva — el flujo normal es event-driven via RabbitMQ.
 */
@FeignClient(name = "ms-cobros", path = "/facturas")
public interface CobrosClient {

    /**
     * GET /facturas/estudiante/{id}/situacion-pago
     * Retorna un map con la clave "situacionPago".
     */
    @GetMapping("/estudiante/{estudianteId}/situacion-pago")
    Map<String, String> obtenerSituacionPago(@PathVariable("estudianteId") Long estudianteId);
}
