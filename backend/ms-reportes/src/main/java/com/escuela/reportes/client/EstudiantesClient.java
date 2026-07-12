package com.escuela.reportes.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "ms-estudiantes", url = "${app.services.estudiantes.url:http://ms-estudiantes:8082}")
public interface EstudiantesClient {

    @GetMapping("/estudiantes/{id}")
    Map<String, Object> obtenerEstudiante(@PathVariable Long id);

    @GetMapping("/estudiantes")
    Page<Map<String, Object>> listarEstudiantes(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size
    );

    @GetMapping("/estudiantes/estado")
    Page<Map<String, Object>> listarPorEstado(
        @RequestParam String estado,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size
    );
}
