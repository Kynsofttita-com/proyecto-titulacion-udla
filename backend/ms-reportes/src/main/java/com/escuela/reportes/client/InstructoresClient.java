package com.escuela.reportes.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.Map;

@FeignClient(name = "ms-instructores", url = "${app.services.instructores.url:http://ms-instructores:8083}")
public interface InstructoresClient {

    @GetMapping("/instructores/{id}")
    Map<String, Object> obtenerInstructor(@PathVariable Long id);

    @GetMapping("/instructores")
    Page<Map<String, Object>> listarInstructores(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size
    );

    @GetMapping("/instructores/{id}/horas")
    Map<String, Object> obtenerHorasTrabajadas(
        @PathVariable Long id,
        @RequestParam(required = false) LocalDate desde,
        @RequestParam(required = false) LocalDate hasta
    );

    @GetMapping("/instructores/{id}/disponibilidad")
    Map<String, Object> obtenerDisponibilidad(@PathVariable Long id);
}
