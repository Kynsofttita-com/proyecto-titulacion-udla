package com.escuela.asignaciones.feign;

import com.escuela.asignaciones.dto.feign.InstructorDetailDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ms-instructores", url = "${FEIGN_MS_INSTRUCTORES_URL:http://localhost:8083}")
public interface InstructorClient {

    @GetMapping("/instructores/{id}")
    InstructorDetailDTO obtenerInstructor(@PathVariable("id") Long id);
}
