package com.escuela.cobros.client;

import com.escuela.cobros.dto.EstudianteDetailDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
    name = "ms-estudiantes",
    url = "${FEIGN_MS_ESTUDIANTES_URL:http://localhost:8082}"
)
public interface EstudianteClient {

    @GetMapping("/estudiantes/{id}")
    EstudianteDetailDTO obtenerEstudiante(@PathVariable Long id);
}
