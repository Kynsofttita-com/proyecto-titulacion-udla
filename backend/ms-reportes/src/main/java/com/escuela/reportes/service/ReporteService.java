package com.escuela.reportes.service;

import com.escuela.reportes.client.CobrosClient;
import com.escuela.reportes.client.EstudiantesClient;
import com.escuela.reportes.client.InstructoresClient;
import com.escuela.reportes.dto.CreateReporteOperativoRequest;
import com.escuela.reportes.dto.DashboardKPIResponse;
import com.escuela.reportes.dto.ReporteFinancieroResponse;
import com.escuela.reportes.dto.ReporteOperativoResponse;
import com.escuela.reportes.entity.EjecucionReporte;
import com.escuela.reportes.repository.EjecucionReporteRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReporteService {

    private final EstudiantesClient estudiantesClient;
    private final CobrosClient cobrosClient;
    private final InstructoresClient instructoresClient;
    private final EjecucionReporteRepository ejecucionReporteRepository;

    @Transactional(readOnly = true)
    public ReporteOperativoResponse generarReporteEstudiantesActivos(CreateReporteOperativoRequest request) {
        long inicio = System.currentTimeMillis();

        try {
            Map<String, Object> datos = new HashMap<>();
            JsonNode response = estudiantesClient.listarEstudiantes(0, 1000);

            List<Map<String, Object>> estudiantesLista = new ArrayList<>();
            long totalActivos = 0;

            if (response != null) {
                // Spring devuelve la respuesta PageImpl como JSON con estructura: { content: [...], totalElements: N, ... }
                if (response.has("content")) {
                    response.get("content").forEach(node ->
                        estudiantesLista.add(new ObjectMapper().convertValue(node, Map.class))
                    );
                }
                if (response.has("totalElements")) {
                    totalActivos = response.get("totalElements").asLong();
                } else if (response.isArray()) {
                    estudiantesLista.clear();
                    response.forEach(node ->
                        estudiantesLista.add(new ObjectMapper().convertValue(node, Map.class))
                    );
                    totalActivos = estudiantesLista.size();
                }
            }

            datos.put("totalActivos", totalActivos);
            datos.put("estudiantes", estudiantesLista);

            long duracion = System.currentTimeMillis() - inicio;
            log.info("Reporte estudiantes_activos generado en {}ms", duracion);

            return new ReporteOperativoResponse(
                "estudiantes_activos",
                datos,
                LocalDateTime.now(),
                duracion,
                (int) totalActivos
            );
        } catch (Exception ex) {
            log.error("Error generando reporte estudiantes_activos", ex);
            throw new RuntimeException("Error generando reporte: " + ex.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public ReporteOperativoResponse generarReporteInstructoresHoras(CreateReporteOperativoRequest request) {
        long inicio = System.currentTimeMillis();

        try {
            Map<String, Object> datos = new HashMap<>();
            JsonNode response = instructoresClient.listarInstructores(0, 100);

            List<Map<String, Object>> instructoresLista = new ArrayList<>();
            long totalInstructores = 0;

            if (response != null) {
                if (response.has("content")) {
                    response.get("content").forEach(node ->
                        instructoresLista.add(new ObjectMapper().convertValue(node, Map.class))
                    );
                }
                if (response.has("totalElements")) {
                    totalInstructores = response.get("totalElements").asLong();
                } else if (response.isArray()) {
                    instructoresLista.clear();
                    response.forEach(node ->
                        instructoresLista.add(new ObjectMapper().convertValue(node, Map.class))
                    );
                    totalInstructores = instructoresLista.size();
                }
            }

            datos.put("totalInstructores", totalInstructores);
            datos.put("instructores", instructoresLista);

            long duracion = System.currentTimeMillis() - inicio;
            log.info("Reporte instructores_horas generado en {}ms", duracion);

            return new ReporteOperativoResponse(
                "instructores_horas",
                datos,
                LocalDateTime.now(),
                duracion,
                (int) totalInstructores
            );
        } catch (Exception ex) {
            log.error("Error generando reporte instructores_horas", ex);
            throw new RuntimeException("Error generando reporte: " + ex.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public ReporteOperativoResponse generarReporteVehiculosSoat(CreateReporteOperativoRequest request) {
        long inicio = System.currentTimeMillis();

        try {
            Map<String, Object> datos = new HashMap<>();
            datos.put("mensaje", "Reporte de vehículos SOAT - Implementar en T10.3");

            long duracion = System.currentTimeMillis() - inicio;
            log.info("Reporte vehiculos_soat generado en {}ms", duracion);

            return new ReporteOperativoResponse(
                "vehiculos_soat",
                datos,
                LocalDateTime.now(),
                duracion,
                0
            );
        } catch (Exception ex) {
            long duracion = System.currentTimeMillis() - inicio;
            log.error("Error generando reporte vehiculos_soat", ex);
            throw new RuntimeException("Error generando reporte: " + ex.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public ReporteOperativoResponse generarReporteAsistencia(CreateReporteOperativoRequest request) {
        long inicio = System.currentTimeMillis();

        try {
            Map<String, Object> datos = new HashMap<>();
            datos.put("mensaje", "Reporte de asistencia - Implementar en T10.3");

            long duracion = System.currentTimeMillis() - inicio;
            log.info("Reporte asistencia generado en {}ms", duracion);

            return new ReporteOperativoResponse(
                "asistencia",
                datos,
                LocalDateTime.now(),
                duracion,
                0
            );
        } catch (Exception ex) {
            long duracion = System.currentTimeMillis() - inicio;
            log.error("Error generando reporte asistencia", ex);
            throw new RuntimeException("Error generando reporte: " + ex.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public ReporteOperativoResponse generarReporteHorasAsignaciones(CreateReporteOperativoRequest request) {
        long inicio = System.currentTimeMillis();

        try {
            Map<String, Object> datos = new HashMap<>();
            datos.put("mensaje", "Reporte de horas asignaciones - Implementar en T10.3");

            long duracion = System.currentTimeMillis() - inicio;
            log.info("Reporte horas_asignaciones generado en {}ms", duracion);

            return new ReporteOperativoResponse(
                "horas_asignaciones",
                datos,
                LocalDateTime.now(),
                duracion,
                0
            );
        } catch (Exception ex) {
            long duracion = System.currentTimeMillis() - inicio;
            log.error("Error generando reporte horas_asignaciones", ex);
            throw new RuntimeException("Error generando reporte: " + ex.getMessage());
        }
    }

    public void registrarEjecucion(String tipoReporte, Long usuarioId, Integer duracionMs, String estado) {
        EjecucionReporte ejecucion = EjecucionReporte.builder()
            .tipoReporte(tipoReporte)
            .usuarioId(usuarioId)
            .duracionMs(duracionMs)
            .estado(estado)
            .fechaEjecucion(LocalDateTime.now())
            .build();

        ejecucionReporteRepository.save(ejecucion);
        log.info("Ejecución de reporte registrada: tipo={}, usuario={}, duracion={}ms",
            tipoReporte, usuarioId, duracionMs);
    }

    @Transactional(readOnly = true)
    public ReporteFinancieroResponse generarReporteIngresoPeriodo(CreateReporteOperativoRequest request) {
        long inicio = System.currentTimeMillis();
        try {
            Map<String, Object> datos = new HashMap<>();
            JsonNode response = cobrosClient.listarPorRango(request.desde(), request.hasta(), 0, 1000);

            List<Map<String, Object>> cobrosList = new ArrayList<>();
            long totalIngresos = 0;
            long totalTransacciones = 0;

            if (response != null && response.has("content")) {
                response.get("content").forEach(node ->
                    cobrosList.add(new ObjectMapper().convertValue(node, Map.class))
                );
                totalTransacciones = response.get("totalElements").asLong(0);
                totalIngresos = cobrosList.stream()
                    .mapToLong(c -> {
                        Object monto = c.getOrDefault("montoOriginal", c.getOrDefault("monto", 0));
                        if (monto instanceof Number) {
                            return ((Number) monto).longValue();
                        }
                        return 0;
                    })
                    .sum();
            }

            List<Map<String, Object>> ingresos = new ArrayList<>();
            for (Map<String, Object> factura : cobrosList) {
                Map<String, Object> ingreso = new HashMap<>();
                ingreso.put("id", factura.get("id"));
                ingreso.put("estudianteNombre", "Estudiante " + factura.get("estudianteId"));
                ingreso.put("concepto", factura.get("tipoPago") != null ? "Pago " + factura.get("tipoPago") : "Pago");
                ingreso.put("monto", factura.get("montoOriginal"));
                ingreso.put("fecha", factura.get("fechaEmision"));
                ingreso.put("estado", factura.get("estado"));
                ingresos.add(ingreso);
            }

            datos.put("totalIngresos", totalIngresos);
            datos.put("totalTransacciones", totalTransacciones);
            datos.put("periodoDesde", request.desde());
            datos.put("periodoHasta", request.hasta());
            datos.put("promedioTransaccion", totalTransacciones > 0 ? totalIngresos / totalTransacciones : 0);
            datos.put("ingresos", ingresos);

            long duracion = System.currentTimeMillis() - inicio;
            log.info("Reporte ingresos_periodo generado en {}ms", duracion);

            return new ReporteFinancieroResponse("ingresos_periodo", datos, LocalDateTime.now(), duracion);
        } catch (Exception ex) {
            log.error("Error generando reporte ingresos_periodo", ex);
            throw new RuntimeException("Error generando reporte: " + ex.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public ReporteFinancieroResponse generarReporteSaldosEstudiante(CreateReporteOperativoRequest request) {
        long inicio = System.currentTimeMillis();
        try {
            Map<String, Object> datos = new HashMap<>();
            datos.put("mensaje", "Reporte de saldos por estudiante - Implementar queries JPA");
            long duracion = System.currentTimeMillis() - inicio;
            log.info("Reporte saldos_estudiante generado en {}ms", duracion);
            return new ReporteFinancieroResponse("saldos_estudiante", datos, LocalDateTime.now(), duracion);
        } catch (Exception ex) {
            log.error("Error generando reporte saldos_estudiante", ex);
            throw new RuntimeException("Error generando reporte: " + ex.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public ReporteFinancieroResponse generarReporteMorosidad(CreateReporteOperativoRequest request) {
        long inicio = System.currentTimeMillis();
        try {
            Map<String, Object> datos = new HashMap<>();
            JsonNode response = cobrosClient.listarPorEstado("VENCIDO", 0, 500);

            List<Map<String, Object>> morosos = new ArrayList<>();
            long totalMorosos = 0;

            if (response != null && response.has("content")) {
                response.get("content").forEach(node ->
                    morosos.add(new ObjectMapper().convertValue(node, Map.class))
                );
                totalMorosos = response.get("totalElements").asLong(0);
            }

            datos.put("totalMorosos", totalMorosos);
            datos.put("cobrosMorosos", morosos);
            long duracion = System.currentTimeMillis() - inicio;
            log.info("Reporte morosidad generado en {}ms", duracion);
            return new ReporteFinancieroResponse("morosidad", datos, LocalDateTime.now(), duracion);
        } catch (Exception ex) {
            log.error("Error generando reporte morosidad", ex);
            throw new RuntimeException("Error generando reporte: " + ex.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public ReporteFinancieroResponse generarReporteRecibos(CreateReporteOperativoRequest request) {
        long inicio = System.currentTimeMillis();
        try {
            Map<String, Object> datos = new HashMap<>();
            JsonNode response = cobrosClient.listarCobros(0, 100);

            List<Map<String, Object>> recibos = new ArrayList<>();
            long totalRecibos = 0;

            if (response != null && response.has("content")) {
                response.get("content").forEach(node -> {
                    Map<String, Object> factura = new ObjectMapper().convertValue(node, Map.class);
                    Map<String, Object> recibo = new HashMap<>();
                    recibo.put("id", factura.get("id"));
                    recibo.put("numero", factura.get("numeroFactura"));
                    recibo.put("estudianteNombre", "Estudiante " + factura.get("estudianteId"));
                    recibo.put("monto", factura.get("montoOriginal"));
                    recibo.put("fechaEmision", factura.get("fechaEmision"));
                    recibo.put("estado", factura.get("estado"));
                    recibos.add(recibo);
                });
                totalRecibos = response.get("totalElements").asLong(0);
            }

            datos.put("totalRecibos", totalRecibos);
            datos.put("recibos", recibos);
            long duracion = System.currentTimeMillis() - inicio;
            log.info("Reporte recibos generado en {}ms", duracion);
            return new ReporteFinancieroResponse("recibos", datos, LocalDateTime.now(), duracion);
        } catch (Exception ex) {
            log.error("Error generando reporte recibos", ex);
            throw new RuntimeException("Error generando reporte: " + ex.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public DashboardKPIResponse generarDashboardKPIs() {
        long inicio = System.currentTimeMillis();
        try {
            Map<String, Object> kpis = new HashMap<>();
            JsonNode estudiantesResponse = estudiantesClient.listarEstudiantes(0, 1);
            JsonNode instructoresResponse = instructoresClient.listarInstructores(0, 1);

            long totalEstudiantes = 0;
            long totalInstructores = 0;

            if (estudiantesResponse != null && estudiantesResponse.has("totalElements")) {
                totalEstudiantes = estudiantesResponse.get("totalElements").asLong(0);
            }
            if (instructoresResponse != null && instructoresResponse.has("totalElements")) {
                totalInstructores = instructoresResponse.get("totalElements").asLong(0);
            }

            kpis.put("totalEstudiantes", totalEstudiantes);
            kpis.put("totalInstructores", totalInstructores);
            kpis.put("tasaAsistencia", 85.5);
            kpis.put("ingresosEsteMes", 0);
            kpis.put("estudiantesActivos", totalEstudiantes);
            kpis.put("horasProgramadas", 0);
            long duracion = System.currentTimeMillis() - inicio;
            log.info("Dashboard KPIs generado en {}ms", duracion);
            return new DashboardKPIResponse(kpis, LocalDateTime.now(), duracion);
        } catch (Exception ex) {
            log.error("Error generando dashboard KPIs", ex);
            throw new RuntimeException("Error generando KPIs: " + ex.getMessage());
        }
    }
}
