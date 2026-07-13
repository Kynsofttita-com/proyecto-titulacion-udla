package com.escuela.reportes.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@FeignClient(name = "ms-instructores", url = "${app.services.instructores.url:http://ms-instructores:8083}")
public interface InstructoresClient {

    @GetMapping("/instructores/{id}")
    JsonNode obtenerInstructor(@PathVariable Long id);

    @GetMapping("/instructores")
    JsonNode listarInstructores(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size
    );

    @GetMapping("/instructores/{id}/horas")
    JsonNode obtenerHorasTrabajadas(
        @PathVariable Long id,
        @RequestParam(required = false) LocalDate desde,
        @RequestParam(required = false) LocalDate hasta
    );

    @GetMapping("/instructores/{id}/disponibilidad")
    JsonNode obtenerDisponibilidad(@PathVariable Long id);
}
