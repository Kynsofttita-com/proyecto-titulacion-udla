package com.escuela.asignaciones.service;

import com.escuela.asignaciones.dto.CreateAsignacionRequest;
import com.escuela.asignaciones.dto.FinalizarAsignacionRequest;
import com.escuela.asignaciones.dto.IniciarAsignacionRequest;
import com.escuela.asignaciones.dto.RecorridoResponse;
import com.escuela.asignaciones.dto.UpdateAsignacionRequest;
import com.escuela.asignaciones.dto.UpdateAsignacionReprogramarRequest;
import com.escuela.asignaciones.dto.AsignacionListResponse;
import com.escuela.asignaciones.dto.AsignacionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AsignacionService {
    Page<AsignacionListResponse> findAll(Pageable pageable);
    Page<AsignacionListResponse> findByEstudianteId(Long estudianteId, Pageable pageable);
    AsignacionResponse findById(Long id);
    AsignacionResponse create(CreateAsignacionRequest request);
    AsignacionResponse update(Long id, UpdateAsignacionRequest request);
    AsignacionResponse reprogramar(Long id, UpdateAsignacionReprogramarRequest request);
    void softDelete(Long id);

    /** Marca la clase como EN_CURSO y registra km_inicial + hora real. */
    RecorridoResponse iniciar(Long id, IniciarAsignacionRequest request);

    /** Marca la clase como COMPLETADA, registra km_final + hora real,
     *  y sincroniza el odómetro del vehículo vía Feign. */
    RecorridoResponse finalizar(Long id, FinalizarAsignacionRequest request);

    /** Resumen del recorrido de una clase (km, duración real, etc). */
    RecorridoResponse obtenerRecorrido(Long id);
}
