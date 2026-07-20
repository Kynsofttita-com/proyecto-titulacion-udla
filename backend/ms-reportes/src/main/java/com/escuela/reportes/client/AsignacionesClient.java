package com.escuela.reportes.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "ms-asignaciones", url = "${app.services.asignaciones.url:http://ms-asignaciones:8085}")
public interface AsignacionesClient {

    @GetMapping("/asignaciones")
    JsonNode listarAsignaciones(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "500") int size
    );
}
