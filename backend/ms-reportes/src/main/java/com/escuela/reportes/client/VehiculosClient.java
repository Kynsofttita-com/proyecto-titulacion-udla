package com.escuela.reportes.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "ms-vehiculos", url = "${app.services.vehiculos.url:http://ms-vehiculos:8085}")
public interface VehiculosClient {

    @GetMapping("/vehiculos")
    JsonNode listarVehiculos(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "100") int size
    );
}
