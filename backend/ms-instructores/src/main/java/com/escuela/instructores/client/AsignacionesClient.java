package com.escuela.instructores.client;

import com.escuela.instructores.dto.HorasCumplidasResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

/**
 * Client Feign hacia ms-asignaciones. Se usa para calcular las horas
 * impartidas por el instructor (clases COMPLETADA) en el resumen.
 */
@FeignClient(name = "ms-asignaciones", url = "${app.services.asignaciones.url:http://ms-asignaciones:8085}")
public interface AsignacionesClient {

    @GetMapping("/asignaciones/instructor/{instructorId}/horas-cumplidas")
    HorasCumplidasResponse horasCumplidasInstructor(
            @PathVariable Long instructorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta);
}
