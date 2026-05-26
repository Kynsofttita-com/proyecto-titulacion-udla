package com.escuela.estudiantes.service;

import com.escuela.estudiantes.dto.CreateEstudianteRequest;
import com.escuela.estudiantes.dto.EstudianteDetailResponse;
import com.escuela.estudiantes.dto.EstudianteListResponse;
import com.escuela.estudiantes.dto.EstudianteResponse;
import com.escuela.estudiantes.dto.UpdateEstudianteRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Contrato del servicio de Estudiantes. Define las operaciones del CRUD
 * + busqueda paginada con filtros.
 */
public interface EstudianteService {

    EstudianteResponse create(CreateEstudianteRequest request);

    EstudianteDetailResponse findById(Long id);

    /**
     * Devuelve el detalle del estudiante asociado al {@code usuario_id} dado.
     * Usado por el endpoint {@code GET /estudiantes/me} para que un estudiante
     * logueado pueda recuperar su propio registro a partir del header
     * {@code X-User-Id} inyectado por el API Gateway.
     */
    EstudianteDetailResponse findByUsuarioId(Long usuarioId);

    Page<EstudianteListResponse> findAll(Pageable pageable, String search, String estado);

    EstudianteResponse update(Long id, UpdateEstudianteRequest request);

    void softDelete(Long id);

    /**
     * Cambia manualmente el estado academico del estudiante validando las
     * transiciones permitidas. Si se provee un motivo, se anexa a las
     * observaciones del estudiante con timestamp.
     */
    EstudianteResponse cambiarEstado(Long id, String nuevoEstado, String motivo);

    /**
     * Suma minutos al acumulado de horas completadas del estudiante.
     * Llamado por ms-asignaciones al finalizar una clase. Si el estudiante
     * alcanza el total requerido por su tipo de curso, transiciona
     * automáticamente a estado COMPLETADO.
     */
    com.escuela.estudiantes.dto.IncrementarHorasResponse incrementarMinutosCompletados(
            Long id, com.escuela.estudiantes.dto.IncrementarHorasRequest request);
}
