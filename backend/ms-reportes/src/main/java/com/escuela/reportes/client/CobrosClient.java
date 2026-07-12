package com.escuela.reportes.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.Map;

@FeignClient(name = "ms-cobros", url = "${app.services.cobros.url:http://ms-cobros:8084}")
public interface CobrosClient {

    @GetMapping("/cobros/estudiante/{id}")
    Map<String, Object> obtenerEstudianteCobros(@PathVariable Long id);

    @GetMapping("/cobros")
    Page<Map<String, Object>> listarCobros(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size
    );

    @GetMapping("/cobros/rango")
    Page<Map<String, Object>> listarPorRango(
        @RequestParam LocalDate desde,
        @RequestParam LocalDate hasta,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size
    );

    @GetMapping("/cobros/estado")
    Page<Map<String, Object>> listarPorEstado(
        @RequestParam String estado,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size
    );
}
