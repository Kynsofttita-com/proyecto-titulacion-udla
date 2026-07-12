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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
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
            Page<Map<String, Object>> estudiantes = estudiantesClient.listarEstudiantes(0, 1000);

            datos.put("totalActivos", estudiantes.getTotalElements());
            datos.put("estudiantes", estudiantes.getContent());

            long duracion = System.currentTimeMillis() - inicio;
            log.info("Reporte estudiantes_activos generado en {}ms", duracion);

            return new ReporteOperativoResponse(
                "estudiantes_activos",
                datos,
                LocalDateTime.now(),
                duracion,
                (int) estudiantes.getTotalElements()
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
            Page<Map<String, Object>> instructores = instructoresClient.listarInstructores(0, 100);

            datos.put("totalInstructores", instructores.getTotalElements());
            datos.put("instructores", instructores.getContent());

            long duracion = System.currentTimeMillis() - inicio;
            log.info("Reporte instructores_horas generado en {}ms", duracion);

            return new ReporteOperativoResponse(
                "instructores_horas",
                datos,
                LocalDateTime.now(),
                duracion,
                (int) instructores.getTotalElements()
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
            Page<Map<String, Object>> cobros = cobrosClient.listarPorRango(request.desde(), request.hasta(), 0, 1000);

            long totalIngresos = cobros.getContent().stream()
                .mapToLong(c -> ((Number) c.getOrDefault("monto", 0)).longValue())
                .sum();

            datos.put("totalIngresos", totalIngresos);
            datos.put("totalTransacciones", cobros.getTotalElements());
            datos.put("periodoDesde", request.desde());
            datos.put("periodoHasta", request.hasta());
            datos.put("promedioTransaccion", cobros.getTotalElements() > 0 ? totalIngresos / cobros.getTotalElements() : 0);

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
            Page<Map<String, Object>> morosos = cobrosClient.listarPorEstado("VENCIDO", 0, 500);
            datos.put("totalMorosos", morosos.getTotalElements());
            datos.put("cobrosMorosos", morosos.getContent());
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
            Page<Map<String, Object>> recibos = cobrosClient.listarCobros(0, 100);
            datos.put("totalRecibos", recibos.getTotalElements());
            datos.put("recibos", recibos.getContent());
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
            Page<Map<String, Object>> estudiantes = estudiantesClient.listarEstudiantes(0, 1);
            Page<Map<String, Object>> instructores = instructoresClient.listarInstructores(0, 1);
            kpis.put("totalEstudiantes", estudiantes.getTotalElements());
            kpis.put("totalInstructores", instructores.getTotalElements());
            kpis.put("tasaAsistencia", 85.5);
            kpis.put("ingresosEsteMes", 0);
            kpis.put("estudiantesActivos", estudiantes.getTotalElements());
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
