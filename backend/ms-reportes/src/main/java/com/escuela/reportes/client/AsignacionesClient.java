package com.escuela.reportes.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@FeignClient(name = "ms-asignaciones", url = "${app.services.asignaciones.url:http://ms-asignaciones:8085}")
public interface AsignacionesClient {

    @GetMapping("/asignaciones")
    JsonNode listarAsignaciones(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "500") int size
    );

    /**
     * Devuelve total de horas de clases COMPLETADA por instructor en un rango.
     * Formato: {"instructorId":..., "clasesCompletadas":..., "minutosCumplidos":..., "horasCumplidas":...}
     */
    @GetMapping("/asignaciones/instructor/{instructorId}/horas-cumplidas")
    JsonNode horasCumplidasInstructor(
        @PathVariable Long instructorId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta
    );
}
