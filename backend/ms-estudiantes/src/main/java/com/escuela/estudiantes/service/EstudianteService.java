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

    Page<EstudianteListResponse> findAll(Pageable pageable, String search, String estado);

    EstudianteResponse update(Long id, UpdateEstudianteRequest request);

    void softDelete(Long id);

    /**
     * Cambia manualmente el estado academico del estudiante validando las
     * transiciones permitidas. Si se provee un motivo, se anexa a las
     * observaciones del estudiante con timestamp.
     */
    EstudianteResponse cambiarEstado(Long id, String nuevoEstado, String motivo);
}
