package com.escuela.reportes.service;

import com.escuela.reportes.client.AsignacionesClient;
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
    private final AsignacionesClient asignacionesClient;
    private final EjecucionReporteRepository ejecucionReporteRepository;

    /**
     * Convierte cualquier representación numérica que venga del JSON (Number,
     * String, null) a BigDecimal. Devuelve ZERO si es null o no parseable.
     */
    private static java.math.BigDecimal toBigDecimal(Object v) {
        if (v == null) return java.math.BigDecimal.ZERO;
        if (v instanceof java.math.BigDecimal bd) return bd;
        if (v instanceof Number n) return new java.math.BigDecimal(n.toString());
        try {
            return new java.math.BigDecimal(v.toString());
        } catch (Exception ex) {
            return java.math.BigDecimal.ZERO;
        }
    }

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

            // Enriquecemos cada instructor con las horas dictadas reales
            // (clases COMPLETADA en el rango). Si el request no trae rango,
            // usamos ultimo dia del mes actual - primer dia del mes actual.
            java.time.LocalDate desde = request != null && request.desde() != null
                    ? request.desde()
                    : java.time.LocalDate.now().withDayOfMonth(1);
            java.time.LocalDate hasta = request != null && request.hasta() != null
                    ? request.hasta()
                    : java.time.LocalDate.now().withDayOfMonth(
                            java.time.LocalDate.now().lengthOfMonth());

            for (Map<String, Object> inst : instructoresLista) {
                Object idObj = inst.get("id");
                if (!(idObj instanceof Number)) {
                    inst.put("horasDictadas", 0);
                    continue;
                }
                Long instructorId = ((Number) idObj).longValue();
                try {
                    JsonNode horas = asignacionesClient.horasCumplidasInstructor(instructorId, desde, hasta);
                    double horasDictadas = horas != null && horas.has("horasCumplidas")
                            ? horas.get("horasCumplidas").asDouble(0)
                            : 0;
                    inst.put("horasDictadas", horasDictadas);
                } catch (Exception ex) {
                    log.debug("No se pudo consultar horas del instructor {}: {}", instructorId, ex.getMessage());
                    inst.put("horasDictadas", 0);
                }
            }

            datos.put("totalInstructores", totalInstructores);
            datos.put("instructores", instructoresLista);
            datos.put("periodoDesde", desde);
            datos.put("periodoHasta", hasta);

            long duracion = System.currentTimeMillis() - inicio;
            log.info("Reporte instructores_horas generado en {}ms (rango {} -> {})",
                    duracion, desde, hasta);

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

            // Paso 1: agregar asignaciones por estudiante desde ms-asignaciones.
            // Programadas = clases con fecha ya pasada (o del dia de hoy) que NO fueron canceladas.
            // Asistidas   = clases COMPLETADA. NO_ASISTIO cuenta como programada pero no asistida.
            // Si el request trae rango de fechas se filtra por él; si no, se toma todo el historial.
            Map<Long, long[]> agregadoPorEstudiante = agregarAsistenciaPorEstudiante(
                request.desde(), request.hasta());

            JsonNode estudiantesResponse = estudiantesClient.listarEstudiantes(0, 1000);
            List<Map<String, Object>> asistencias = new ArrayList<>();
            long totalAsistencias = 0;

            if (estudiantesResponse != null && estudiantesResponse.has("content")) {
                for (JsonNode node : estudiantesResponse.get("content")) {
                    Map<String, Object> asistencia = new HashMap<>();
                    Long estId = node.has("id") ? node.get("id").asLong() : null;
                    // El listado paginado de ms-estudiantes devuelve nombreCompleto,
                    // no nombre/apellido separados. Nos apoyamos en eso y damos
                    // fallback a los campos separados por si el DTO cambia.
                    String nombreCompleto = node.has("nombreCompleto") && !node.get("nombreCompleto").isNull()
                        ? node.get("nombreCompleto").asText()
                        : ((node.has("nombre") ? node.get("nombre").asText("") : "")
                            + " "
                            + (node.has("apellido") ? node.get("apellido").asText("") : "")).trim();

                    long[] contadores = agregadoPorEstudiante.getOrDefault(estId, new long[]{0L, 0L});
                    long programadas = contadores[0];
                    long asistidas = contadores[1];
                    double porcentaje = programadas > 0
                        ? Math.round((asistidas * 1000.0 / programadas)) / 10.0
                        : 0.0;

                    asistencia.put("estudianteId", estId);
                    asistencia.put("estudianteNombre", nombreCompleto);
                    asistencia.put("nombreCompleto", nombreCompleto);
                    asistencia.put("cedula", node.has("cedula") ? node.get("cedula").asText("") : "");
                    asistencia.put("telefono", node.has("telefono") ? node.get("telefono").asText("") : "");
                    asistencia.put("estado", node.has("estado") ? node.get("estado").asText("") : "");
                    asistencia.put("clases_programadas", programadas);
                    asistencia.put("clases_asistidas", asistidas);
                    asistencia.put("porcentaje_asistencia", porcentaje);
                    asistencias.add(asistencia);
                }
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

    /**
     * Recorre las asignaciones paginadas de ms-asignaciones y devuelve por
     * estudianteId un par [programadas, asistidas]. "Programada" = clase
     * agendada en el sistema (cualquier estado excepto CANCELADA), incluye
     * futuras. "Asistida" = solo estado COMPLETADA. Si el request trae
     * desde/hasta se filtra por rango; sin rango se cuenta todo el historial.
     */
    private Map<Long, long[]> agregarAsistenciaPorEstudiante(
            java.time.LocalDate desde, java.time.LocalDate hasta) {
        Map<Long, long[]> agregado = new HashMap<>();
        int page = 0;
        int size = 500;
        int totalPages = 1;

        while (page < totalPages) {
            JsonNode response = asignacionesClient.listarAsignaciones(page, size);
            if (response == null || !response.has("content")) break;
            totalPages = response.has("totalPages") ? response.get("totalPages").asInt(1) : 1;

            for (JsonNode node : response.get("content")) {
                String estado = node.has("estado") ? node.get("estado").asText("") : "";
                if ("CANCELADA".equals(estado)) continue;

                if (desde != null || hasta != null) {
                    String fechaStr = node.has("fecha") && !node.get("fecha").isNull()
                        ? node.get("fecha").asText(null) : null;
                    if (fechaStr == null) continue;
                    java.time.LocalDate fecha;
                    try {
                        fecha = java.time.LocalDate.parse(fechaStr);
                    } catch (Exception ex) {
                        continue;
                    }
                    if (desde != null && fecha.isBefore(desde)) continue;
                    if (hasta != null && fecha.isAfter(hasta)) continue;
                }

                Long estId = node.has("estudianteId") && !node.get("estudianteId").isNull()
                    ? node.get("estudianteId").asLong() : null;
                if (estId == null) continue;

                long[] contadores = agregado.computeIfAbsent(estId, k -> new long[]{0L, 0L});
                contadores[0]++;
                if ("COMPLETADA".equals(estado)) contadores[1]++;
            }
            page++;
        }
        return agregado;
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
            long totalTransacciones = 0;

            if (response != null && response.has("content")) {
                response.get("content").forEach(node ->
                    cobrosList.add(new ObjectMapper().convertValue(node, Map.class))
                );
                totalTransacciones = response.get("totalElements").asLong(0);
            }

            // Totales desglosados por lo que se facturó vs lo que realmente entró en caja.
            java.math.BigDecimal totalFacturado = java.math.BigDecimal.ZERO;
            java.math.BigDecimal totalCobrado   = java.math.BigDecimal.ZERO;
            java.math.BigDecimal totalPorCobrar = java.math.BigDecimal.ZERO;
            long facturasAnuladas = 0;

            List<Map<String, Object>> ingresos = new ArrayList<>();
            for (Map<String, Object> factura : cobrosList) {
                Map<String, Object> ingreso = new HashMap<>();
                ingreso.put("id", factura.get("id"));
                ingreso.put("numeroFactura", factura.get("numeroFactura"));
                Long estudianteId = ((Number) factura.get("estudianteId")).longValue();
                ingreso.put("estudianteId", estudianteId);
                ingreso.put("estudianteNombre", obtenerNombreEstudiante(estudianteId));

                String tipoPago = factura.get("tipoPago") != null ? factura.get("tipoPago").toString() : "CONTADO";
                ingreso.put("tipoPago", tipoPago);
                // Se conserva "concepto" por compatibilidad con export/exporter, pero ya
                // no es el eje principal — ahora usamos montoFacturado/cobrado/saldo.
                ingreso.put("concepto", "Factura " + tipoPago);

                java.math.BigDecimal montoOriginal = toBigDecimal(factura.get("montoOriginal"));
                java.math.BigDecimal montoPagado   = toBigDecimal(factura.get("montoPagado"));
                java.math.BigDecimal saldo         = toBigDecimal(factura.get("saldo"));
                String estado = factura.get("estado") != null ? factura.get("estado").toString() : "";

                ingreso.put("montoFacturado", montoOriginal);
                ingreso.put("montoCobrado", montoPagado);
                ingreso.put("saldo", saldo);
                // Alias legacy: el frontend viejo espera "monto"; lo mantenemos = facturado.
                ingreso.put("monto", montoOriginal);
                ingreso.put("fecha", factura.get("fechaEmision"));
                ingreso.put("fechaEmision", factura.get("fechaEmision"));
                ingreso.put("fechaVencimiento", factura.get("fechaVencimiento"));
                ingreso.put("estado", estado);
                ingreso.put("numeroCuotas", factura.get("numeroCuotas"));
                ingreso.put("cuotasPagadas", factura.get("cuotasPagadas"));
                ingresos.add(ingreso);

                if ("ANULADA".equals(estado)) {
                    facturasAnuladas++;
                    // ANULADA no cuenta en facturado ni en por cobrar (se descarta del ciclo).
                    continue;
                }
                totalFacturado = totalFacturado.add(montoOriginal);
                totalCobrado   = totalCobrado.add(montoPagado);
                totalPorCobrar = totalPorCobrar.add(saldo);
            }

            java.math.BigDecimal porcentajeCobrado = totalFacturado.signum() > 0
                ? totalCobrado.multiply(java.math.BigDecimal.valueOf(100))
                    .divide(totalFacturado, 2, java.math.RoundingMode.HALF_UP)
                : java.math.BigDecimal.ZERO;

            long facturasActivas = totalTransacciones - facturasAnuladas;

            // Legacy: totalIngresos = totalFacturado (para no romper clientes viejos).
            datos.put("totalIngresos", totalFacturado);
            datos.put("totalFacturado", totalFacturado);
            datos.put("totalCobrado", totalCobrado);
            datos.put("totalPorCobrar", totalPorCobrar);
            datos.put("porcentajeCobrado", porcentajeCobrado);
            datos.put("totalTransacciones", totalTransacciones);
            datos.put("facturasActivas", facturasActivas);
            datos.put("facturasAnuladas", facturasAnuladas);
            datos.put("periodoDesde", request.desde());
            datos.put("periodoHasta", request.hasta());
            datos.put("promedioTransaccion", facturasActivas > 0
                ? totalFacturado.divide(java.math.BigDecimal.valueOf(facturasActivas), 2, java.math.RoundingMode.HALF_UP)
                : java.math.BigDecimal.ZERO);
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
                    // Se considera morosa desde el DIA de vencimiento inclusive: si vence
                    // hoy y no se pago, aparece con 0 dias de atraso. Alinea el criterio
                    // del reporte con el badge rojo "Pendiente de pago" en la ficha.
                    if (fechaVenc.isAfter(hoy)) continue;

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
