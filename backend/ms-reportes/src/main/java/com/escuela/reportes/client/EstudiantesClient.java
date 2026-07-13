package com.escuela.reportes.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "ms-estudiantes", url = "${app.services.estudiantes.url:http://ms-estudiantes:8082}")
public interface EstudiantesClient {

    @GetMapping("/estudiantes/{id}")
    JsonNode obtenerEstudiante(@PathVariable Long id);

    @GetMapping("/estudiantes")
    JsonNode listarEstudiantes(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size
    );

    @GetMapping("/estudiantes/estado")
    JsonNode listarPorEstado(
        @RequestParam String estado,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size
    );
}
