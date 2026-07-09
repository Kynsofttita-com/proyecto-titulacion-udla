package com.escuela.reportes.controller;

import com.escuela.reportes.dto.CreateReporteOperativoRequest;
import com.escuela.reportes.dto.ReporteOperativoResponse;
import com.escuela.reportes.service.ReporteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService service;

    @PostMapping("/estudiantes-activos")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ReporteOperativoResponse> generarReporteEstudiantesActivos(
        @Valid @RequestBody CreateReporteOperativoRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            service.generarReporteEstudiantesActivos(request)
        );
    }

    @PostMapping("/instructores-horas")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ReporteOperativoResponse> generarReporteInstructoresHoras(
        @Valid @RequestBody CreateReporteOperativoRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            service.generarReporteInstructoresHoras(request)
        );
    }

    @PostMapping("/vehiculos-soat")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ReporteOperativoResponse> generarReporteVehiculosSoat(
        @Valid @RequestBody CreateReporteOperativoRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            service.generarReporteVehiculosSoat(request)
        );
    }

    @PostMapping("/asistencia")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ReporteOperativoResponse> generarReporteAsistencia(
        @Valid @RequestBody CreateReporteOperativoRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            service.generarReporteAsistencia(request)
        );
    }

    @PostMapping("/horas-asignaciones")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ReporteOperativoResponse> generarReporteHorasAsignaciones(
        @Valid @RequestBody CreateReporteOperativoRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            service.generarReporteHorasAsignaciones(request)
        );
    }
}
