package com.escuela.reportes.controller;

import com.escuela.reportes.dto.CreateReporteOperativoRequest;
import com.escuela.reportes.dto.DashboardKPIResponse;
import com.escuela.reportes.dto.ReporteFinancieroResponse;
import com.escuela.reportes.dto.ReporteOperativoResponse;
import com.escuela.reportes.service.ReporteExportService;
import com.escuela.reportes.service.ReporteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService service;
    private final ReporteExportService exportService;

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

    @PostMapping("/ingresos-periodo")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ReporteFinancieroResponse> generarReporteIngresoPeriodo(
        @Valid @RequestBody CreateReporteOperativoRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            service.generarReporteIngresoPeriodo(request)
        );
    }

    @PostMapping("/saldos-estudiante")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ReporteFinancieroResponse> generarReporteSaldosEstudiante(
        @Valid @RequestBody CreateReporteOperativoRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            service.generarReporteSaldosEstudiante(request)
        );
    }

    @PostMapping("/morosidad")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ReporteFinancieroResponse> generarReporteMorosidad(
        @Valid @RequestBody CreateReporteOperativoRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            service.generarReporteMorosidad(request)
        );
    }

    @PostMapping("/recibos")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ReporteFinancieroResponse> generarReporteRecibos(
        @Valid @RequestBody CreateReporteOperativoRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            service.generarReporteRecibos(request)
        );
    }

    @GetMapping("/kpis")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<DashboardKPIResponse> obtenerDashboardKPIs() {
        return ResponseEntity.ok(service.generarDashboardKPIs());
    }

    @PostMapping("/exportar/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<byte[]> exportarAPDF(
        @RequestParam String titulo,
        @RequestBody List<Map<String, Object>> datos
    ) {
        byte[] pdf = exportService.exportarAPDF(titulo, datos);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.builder("attachment")
            .filename(titulo.replace(" ", "_") + ".pdf")
            .build());
        headers.set("Content-Type", "application/pdf");
        return ResponseEntity.ok()
            .headers(headers)
            .body(pdf);
    }

    @PostMapping("/exportar/excel")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<byte[]> exportarAExcel(
        @RequestParam String titulo,
        @RequestBody Map<String, Object> datos
    ) {
        byte[] excel = exportService.exportarAExcel(titulo, datos);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.builder("attachment")
            .filename(titulo.replace(" ", "_") + ".xlsx")
            .build());
        headers.set("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        return ResponseEntity.ok()
            .headers(headers)
            .body(excel);
    }

    @PostMapping("/exportar/csv")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<byte[]> exportarACSV(
        @RequestParam String titulo,
        @RequestBody Map<String, Object> datos
    ) {
        byte[] csv = exportService.exportarACSV(titulo, datos);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.builder("attachment")
            .filename(titulo.replace(" ", "_") + ".csv")
            .build());
        headers.set("Content-Type", "text/csv");
        return ResponseEntity.ok()
            .headers(headers)
            .body(csv);
    }
}
