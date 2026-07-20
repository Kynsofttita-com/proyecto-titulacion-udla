package com.escuela.reportes.service;

import com.escuela.reportes.client.AsignacionesClient;
import com.escuela.reportes.client.CobrosClient;
import com.escuela.reportes.client.EstudiantesClient;
import com.escuela.reportes.client.InstructoresClient;
import com.escuela.reportes.client.VehiculosClient;
import com.escuela.reportes.dto.CreateReporteOperativoRequest;
import com.escuela.reportes.dto.ReporteOperativoResponse;
import com.escuela.reportes.repository.EjecucionReporteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

    @Mock
    private EstudiantesClient estudiantesClient;

    @Mock
    private CobrosClient cobrosClient;

    @Mock
    private InstructoresClient instructoresClient;

    @Mock
    private VehiculosClient vehiculosClient;

    @Mock
    private AsignacionesClient asignacionesClient;

    @Mock
    private EjecucionReporteRepository ejecucionReporteRepository;

    @InjectMocks
    private ReporteService service;

    private CreateReporteOperativoRequest request;

    @BeforeEach
    void setUp() {
        request = new CreateReporteOperativoRequest(
            "estudiantes_activos",
            LocalDate.now().minusMonths(1),
            LocalDate.now(),
            null
        );
    }

    @Test
    void testGenerarReporteEstudiantesActivos() {
        List<Map<String, Object>> estudiantes = new ArrayList<>();
        Map<String, Object> est = new HashMap<>();
        est.put("id", 1L);
        est.put("nombre", "Juan Pérez");
        estudiantes.add(est);

        Map<String, Object> pageJson = new HashMap<>();
        pageJson.put("content", estudiantes);
        pageJson.put("totalElements", 1L);
        JsonNode jsonNode = new ObjectMapper().valueToTree(pageJson);
        when(estudiantesClient.listarEstudiantes(0, 1000)).thenReturn(jsonNode);

        ReporteOperativoResponse response = service.generarReporteEstudiantesActivos(request);

        assertNotNull(response);
        assertEquals("estudiantes_activos", response.tipoReporte());
        assertEquals(1L, response.totalRegistros().longValue());
        assertTrue(response.duracionMs() >= 0);
        verify(estudiantesClient).listarEstudiantes(0, 1000);
    }

    @Test
    void testGenerarReporteInstructoresHoras() {
        List<Map<String, Object>> instructores = new ArrayList<>();
        Map<String, Object> inst = new HashMap<>();
        inst.put("id", 1L);
        inst.put("nombre", "Carlos López");
        instructores.add(inst);

        Map<String, Object> pageJson = new HashMap<>();
        pageJson.put("content", instructores);
        pageJson.put("totalElements", 1L);
        JsonNode jsonNode = new ObjectMapper().valueToTree(pageJson);
        when(instructoresClient.listarInstructores(0, 100)).thenReturn(jsonNode);

        ReporteOperativoResponse response = service.generarReporteInstructoresHoras(request);

        assertNotNull(response);
        assertEquals("instructores_horas", response.tipoReporte());
        assertEquals(1L, response.totalRegistros().longValue());
        verify(instructoresClient).listarInstructores(0, 100);
    }

    @Test
    void testGenerarReporteVehiculosSoat() {
        List<Map<String, Object>> vehiculos = new ArrayList<>();
        Map<String, Object> veh = new HashMap<>();
        veh.put("id", 1L);
        veh.put("placa", "ABC-1234");
        veh.put("marca", "Toyota");
        vehiculos.add(veh);

        Map<String, Object> pageData = new HashMap<>();
        pageData.put("content", vehiculos);
        pageData.put("totalElements", 1L);

        JsonNode jsonNode = new ObjectMapper().valueToTree(pageData);
        when(vehiculosClient.listarVehiculos(0, 100)).thenReturn(jsonNode);

        ReporteOperativoResponse response = service.generarReporteVehiculosSoat(request);

        assertNotNull(response);
        assertEquals("vehiculos_soat", response.tipoReporte());
        assertEquals(1, response.totalRegistros().intValue());
        verify(vehiculosClient).listarVehiculos(0, 100);
    }

    @Test
    void testGenerarReporteAsistencia() {
        ReporteOperativoResponse response = service.generarReporteAsistencia(request);

        assertNotNull(response);
        assertEquals("asistencia", response.tipoReporte());
        assertEquals(0, response.totalRegistros().intValue());
    }

    @Test
    void testGenerarReporteHorasAsignaciones() {
        ReporteOperativoResponse response = service.generarReporteHorasAsignaciones(request);

        assertNotNull(response);
        assertEquals("horas_asignaciones", response.tipoReporte());
        assertEquals(0, response.totalRegistros().intValue());
    }

    @Test
    void testRegistrarEjecucion() {
        service.registrarEjecucion("estudiantes_activos", 100L, 250, "EXITOSO");

        verify(ejecucionReporteRepository).save(any());
    }

    @Test
    void testGenerarReporteEstudiantesActivos_Error() {
        when(estudiantesClient.listarEstudiantes(anyInt(), anyInt()))
            .thenThrow(new RuntimeException("Connection timeout"));

        assertThrows(RuntimeException.class, () -> service.generarReporteEstudiantesActivos(request));
    }
}
