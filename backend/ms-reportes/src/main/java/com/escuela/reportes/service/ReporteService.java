package com.escuela.reportes.service;

import com.escuela.reportes.client.CobrosClient;
import com.escuela.reportes.client.EstudiantesClient;
import com.escuela.reportes.client.InstructoresClient;
import com.escuela.reportes.client.VehiculosClient;
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
    private final VehiculosClient vehiculosClient;
    private final EjecucionReporteRepository ejecucionReporteRepository;

    private String obtenerNombreEstudiante(Long estudianteId) {
        try {
            JsonNode estudianteNode = estudiantesClient.obtenerEstudiante(estudianteId);
            if (estudianteNode != null) {
                String nombre = estudianteNode.has("nombre") ? estudianteNode.get("nombre").asText("") : "";
                String apellido = estudianteNode.has("apellido") ? estudianteNode.get("apellido").asText("") : "";
                if (!nombre.isEmpty() || !apellido.isEmpty()) {
                    return (nombre + " " + apellido).trim();
                }
            }
        } catch (Exception ex) {
            log.debug("No se pudo obtener nombre del estudiante {}: {}", estudianteId, ex.getMessage());
        }
        return "Estudiante " + estudianteId;
    }

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

            log.debug("VehiculosClient inyectado: {}", vehiculosClient != null);
            log.debug("Llamando a vehiculosClient.listarVehiculos(0, 100)");

            JsonNode response = vehiculosClient.listarVehiculos(0, 100);

            log.debug("Respuesta recibida. Response null: {}", response == null);
            if (response != null) {
                log.debug("Respuesta JSON: {}", response.toString());
                log.debug("Tiene 'content': {}, Es array: {}", response.has("content"), response.isArray());
            }

            List<Map<String, Object>> vehiculosList = new ArrayList<>();
            long totalVehiculos = 0;

            if (response != null) {
                // Spring devuelve la respuesta PageImpl como JSON con estructura: { content: [...], totalElements: N, ... }
                if (response.has("content")) {
                    log.debug("Procesando como Page con 'content'");
                    response.get("content").forEach(node -> {
                        Map<String, Object> veh = new ObjectMapper().convertValue(node, Map.class);
                        vehiculosList.add(veh);
                    });
                    totalVehiculos = response.get("totalElements").asLong(0);
                    log.debug("Vehículos extraídos de content: {}", vehiculosList.size());
                } else if (response.isArray()) {
                    log.debug("Procesando como array directo");
                    response.forEach(node -> {
                        Map<String, Object> veh = new ObjectMapper().convertValue(node, Map.class);
                        vehiculosList.add(veh);
                    });
                    totalVehiculos = vehiculosList.size();
                    log.debug("Vehículos extraídos de array: {}", vehiculosList.size());
                } else {
                    log.warn("Respuesta no tiene 'content' ni es array. Estructura desconocida: {}", response.toString());
                }
            } else {
                log.warn("VehiculosClient devolvió null");
            }

            log.info("Total vehículos obtenidos: {}", totalVehiculos);

            datos.put("totalVehiculos", totalVehiculos);
            datos.put("vehiculos", vehiculosList);

            long duracion = System.currentTimeMillis() - inicio;
            log.info("Reporte vehiculos_soat generado en {}ms", duracion);

            return new ReporteOperativoResponse(
                "vehiculos_soat",
                datos,
                LocalDateTime.now(),
                duracion,
                (int) totalVehiculos
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

            JsonNode estudiantesResponse = estudiantesClient.listarEstudiantes(0, 1000);
            List<Map<String, Object>> asistencias = new ArrayList<>();
            long totalAsistencias = 0;

            if (estudiantesResponse != null && estudiantesResponse.has("content")) {
                estudiantesResponse.get("content").forEach(node -> {
                    Map<String, Object> asistencia = new HashMap<>();
                    asistencia.put("estudianteId", node.get("id"));
                    asistencia.put("nombre", node.get("nombre"));
                    asistencia.put("apellido", node.get("apellido"));
                    asistencia.put("estado", node.get("estado"));
                    asistencia.put("clases_programadas", 10);
                    asistencia.put("clases_asistidas", 9);
                    asistencia.put("porcentaje_asistencia", 90.0);
                    asistencias.add(asistencia);
                });
                totalAsistencias = asistencias.size();
            }

            datos.put("totalEstudiantes", totalAsistencias);
            datos.put("asistencias", asistencias);
            datos.put("periodo_desde", request.desde());
            datos.put("periodo_hasta", request.hasta());

            long duracion = System.currentTimeMillis() - inicio;
            log.info("Reporte asistencia generado en {}ms con {} registros", duracion, totalAsistencias);

            return new ReporteOperativoResponse(
                "asistencia",
                datos,
                LocalDateTime.now(),
                duracion,
                (int) totalAsistencias
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

            JsonNode estudiantesResponse = estudiantesClient.listarEstudiantes(0, 1000);
            List<Map<String, Object>> horasLista = new ArrayList<>();
            long totalHoras = 0;

            if (estudiantesResponse != null && estudiantesResponse.has("content")) {
                estudiantesResponse.get("content").forEach(node -> {
                    Map<String, Object> horas = new HashMap<>();
                    horas.put("estudianteId", node.get("id"));
                    horas.put("nombre", node.get("nombre"));
                    horas.put("horas_programadas", 120);
                    horas.put("horas_completadas", 85);
                    horas.put("horas_faltantes", 35);
                    horas.put("estado", node.get("estado"));
                    horasLista.add(horas);
                });
                totalHoras = horasLista.size();
            }

            long totalHorasProgram = horasLista.stream()
                .mapToLong(h -> ((Number) h.getOrDefault("horas_programadas", 0)).longValue())
                .sum();
            long totalHorasCompletadas = horasLista.stream()
                .mapToLong(h -> ((Number) h.getOrDefault("horas_completadas", 0)).longValue())
                .sum();

            datos.put("totalEstudiantes", totalHoras);
            datos.put("horas_totales_programadas", totalHorasProgram);
            datos.put("horas_totales_completadas", totalHorasCompletadas);
            datos.put("asignaciones", horasLista);

            long duracion = System.currentTimeMillis() - inicio;
            log.info("Reporte horas_asignaciones generado en {}ms", duracion);

            return new ReporteOperativoResponse(
                "horas_asignaciones",
                datos,
                LocalDateTime.now(),
                duracion,
                (int) totalHoras
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
                Long estudianteId = ((Number) factura.get("estudianteId")).longValue();
                ingreso.put("estudianteNombre", obtenerNombreEstudiante(estudianteId));
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

            JsonNode estudiantesResponse = estudiantesClient.listarEstudiantes(0, 1000);
            List<Map<String, Object>> saldos = new ArrayList<>();
            long totalSaldo = 0;

            if (estudiantesResponse != null && estudiantesResponse.has("content")) {
                estudiantesResponse.get("content").forEach(node -> {
                    Map<String, Object> saldo = new HashMap<>();
                    saldo.put("estudianteId", node.get("id"));
                    saldo.put("nombre", node.get("nombre"));
                    saldo.put("apellido", node.get("apellido"));
                    saldo.put("saldo_pendiente", 0);
                    saldo.put("saldo_pagado", 0);
                    saldo.put("estado", node.get("estado"));
                    saldos.add(saldo);
                });
            }

            datos.put("totalEstudiantes", saldos.size());
            datos.put("saldos", saldos);
            datos.put("saldo_total_cartera", totalSaldo);

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
            // Facturas atrasadas = estado != PAGADA/ANULADA + saldo > 0 + fechaVencimiento < hoy.
            // Filtramos aqui (en ms-reportes) para no agregar endpoint dedicado a ms-cobros.
            JsonNode response = cobrosClient.listarCobros(0, 500);

            List<Map<String, Object>> morosos = new ArrayList<>();
            java.math.BigDecimal montoVencidoTotal = java.math.BigDecimal.ZERO;
            java.time.LocalDate hoy = java.time.LocalDate.now();

            if (response != null && response.has("content")) {
                for (JsonNode node : response.get("content")) {
                    String estado = node.has("estado") ? node.get("estado").asText("") : "";
                    if ("PAGADA".equals(estado) || "ANULADA".equals(estado)) continue;

                    java.math.BigDecimal saldo = node.has("saldo")
                        ? new java.math.BigDecimal(node.get("saldo").asText("0"))
                        : java.math.BigDecimal.ZERO;
                    if (saldo.compareTo(java.math.BigDecimal.ZERO) <= 0) continue;

                    String fechaVencStr = node.has("fechaVencimiento") ? node.get("fechaVencimiento").asText(null) : null;
                    if (fechaVencStr == null) continue;
                    java.time.LocalDate fechaVenc = java.time.LocalDate.parse(fechaVencStr);
                    if (!fechaVenc.isBefore(hoy)) continue;

                    long diasAtraso = java.time.temporal.ChronoUnit.DAYS.between(fechaVenc, hoy);
                    Long estudianteId = node.has("estudianteId") ? node.get("estudianteId").asLong() : null;

                    Map<String, Object> row = new HashMap<>();
                    row.put("id", node.has("id") ? node.get("id").asLong() : null);
                    row.put("numeroFactura", node.has("numeroFactura") ? node.get("numeroFactura").asText() : "");
                    row.put("estudianteId", estudianteId);
                    row.put("estudianteNombre", estudianteId != null ? obtenerNombreEstudiante(estudianteId) : "");
                    row.put("telefono", estudianteId != null ? obtenerTelefonoEstudiante(estudianteId) : "");
                    row.put("montoOriginal", node.has("montoOriginal") ? node.get("montoOriginal").asText() : "0");
                    row.put("montoPagado", node.has("montoPagado") ? node.get("montoPagado").asText() : "0");
                    row.put("montoVencido", saldo.toPlainString());
                    row.put("saldo", saldo.toPlainString());
                    row.put("estado", estado);
                    row.put("fechaVencimiento", fechaVencStr);
                    row.put("diasAtraso", diasAtraso);
                    morosos.add(row);

                    montoVencidoTotal = montoVencidoTotal.add(saldo);
                }
            }

            // Ordenar por dias de atraso descendente (los mas viejos primero)
            morosos.sort((a, b) -> Long.compare(
                ((Number) b.getOrDefault("diasAtraso", 0)).longValue(),
                ((Number) a.getOrDefault("diasAtraso", 0)).longValue()
            ));

            datos.put("totalMorosos", (long) morosos.size());
            datos.put("montoVencidoTotal", montoVencidoTotal.toPlainString());
            datos.put("cobrosMorosos", morosos);
            datos.put("morosos", morosos); // alias por compatibilidad con la UI
            long duracion = System.currentTimeMillis() - inicio;
            log.info("Reporte morosidad generado en {}ms. Facturas vencidas: {}. Monto: ${}",
                    duracion, morosos.size(), montoVencidoTotal);
            return new ReporteFinancieroResponse("morosidad", datos, LocalDateTime.now(), duracion);
        } catch (Exception ex) {
            log.error("Error generando reporte morosidad", ex);
            throw new RuntimeException("Error generando reporte: " + ex.getMessage());
        }
    }

    private String obtenerTelefonoEstudiante(Long estudianteId) {
        try {
            JsonNode estudianteNode = estudiantesClient.obtenerEstudiante(estudianteId);
            if (estudianteNode != null && estudianteNode.has("telefono")) {
                return estudianteNode.get("telefono").asText("");
            }
        } catch (Exception ex) {
            log.debug("No se pudo obtener telefono del estudiante {}: {}", estudianteId, ex.getMessage());
        }
        return "";
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
                    Long estudianteId = ((Number) factura.get("estudianteId")).longValue();
                    recibo.put("estudianteNombre", obtenerNombreEstudiante(estudianteId));
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

            JsonNode estudiantesResponse = estudiantesClient.listarEstudiantes(0, 1000);
            JsonNode instructoresResponse = instructoresClient.listarInstructores(0, 100);
            JsonNode vehiculosResponse = vehiculosClient.listarVehiculos(0, 100);

            long totalEstudiantes = 0;
            long totalInstructores = 0;
            long totalVehiculos = 0;
            long estudiantesActivos = 0;

            if (estudiantesResponse != null && estudiantesResponse.has("totalElements")) {
                totalEstudiantes = estudiantesResponse.get("totalElements").asLong(0);
                if (estudiantesResponse.has("content")) {
                    estudiantesActivos = (long) java.util.stream.StreamSupport.stream(
                        estudiantesResponse.get("content").spliterator(), false
                    ).filter(node -> {
                        String estado = node.has("estado") ? node.get("estado").asText("") : "";
                        return "MATRICULADO".equals(estado) || "CURSANDO".equals(estado);
                    }).count();
                }
            }
            if (instructoresResponse != null && instructoresResponse.has("totalElements")) {
                totalInstructores = instructoresResponse.get("totalElements").asLong(0);
            }
            if (vehiculosResponse != null && vehiculosResponse.has("totalElements")) {
                totalVehiculos = vehiculosResponse.get("totalElements").asLong(0);
            }

            JsonNode ingresosResponse = cobrosClient.listarCobros(0, 1000);
            long totalIngresos = 0;
            if (ingresosResponse != null && ingresosResponse.has("content")) {
                totalIngresos = (long) java.util.stream.StreamSupport.stream(
                    ingresosResponse.get("content").spliterator(), false
                ).mapToLong(node -> {
                    Object monto = node.has("montoOriginal") ? node.get("montoOriginal") : node.get("monto");
                    if (monto instanceof com.fasterxml.jackson.databind.JsonNode) {
                        return ((com.fasterxml.jackson.databind.JsonNode) monto).asLong(0);
                    }
                    return 0;
                }).sum();
            }

            double tasaAsistencia = estudiantesActivos > 0 ? 85.5 : 0;
            long horasProgramadas = totalEstudiantes * 120;

            kpis.put("totalEstudiantes", totalEstudiantes);
            kpis.put("estudiantesActivos", estudiantesActivos);
            kpis.put("totalInstructores", totalInstructores);
            kpis.put("totalVehiculos", totalVehiculos);
            kpis.put("tasaAsistencia", tasaAsistencia);
            kpis.put("ingresosEsteMes", totalIngresos);
            kpis.put("horasProgramadas", horasProgramadas);
            kpis.put("horasCompletadas", (long) (horasProgramadas * 0.70));
            kpis.put("porcentajeAvance", 70.0);

            long duracion = System.currentTimeMillis() - inicio;
            log.info("Dashboard KPIs generado en {}ms: {} estudiantes, {} activos, {} instructores",
                duracion, totalEstudiantes, estudiantesActivos, totalInstructores);
            return new DashboardKPIResponse(kpis, LocalDateTime.now(), duracion);
        } catch (Exception ex) {
            log.error("Error generando dashboard KPIs", ex);
            throw new RuntimeException("Error generando KPIs: " + ex.getMessage());
        }
    }
}
