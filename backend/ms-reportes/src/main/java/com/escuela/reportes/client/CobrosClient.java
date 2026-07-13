package com.escuela.reportes.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@FeignClient(name = "ms-cobros", url = "${app.services.cobros.url:http://ms-cobros:8086}")
public interface CobrosClient {

    @GetMapping("/facturas/estudiante/{id}")
    JsonNode obtenerEstudianteCobros(@PathVariable Long id);

    @GetMapping("/facturas")
    JsonNode listarCobros(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size
    );

    @GetMapping("/facturas")
    JsonNode listarPorRango(
        @RequestParam(required = false) LocalDate desde,
        @RequestParam(required = false) LocalDate hasta,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size
    );

    @GetMapping("/pagos")
    JsonNode listarPorEstado(
        @RequestParam String estado,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size
    );
}
