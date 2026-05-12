package com.escuela.estudiantes.mapper;

import com.escuela.estudiantes.dto.ContactoEmergenciaRequest;
import com.escuela.estudiantes.dto.ContactoEmergenciaResponse;
import com.escuela.estudiantes.dto.CreateEstudianteRequest;
import com.escuela.estudiantes.dto.DocumentoResponse;
import com.escuela.estudiantes.dto.EstudianteDetailResponse;
import com.escuela.estudiantes.dto.EstudianteListResponse;
import com.escuela.estudiantes.dto.EstudianteResponse;
import com.escuela.estudiantes.dto.UpdateEstudianteRequest;
import com.escuela.estudiantes.entity.ContactoEmergencia;
import com.escuela.estudiantes.entity.Documento;
import com.escuela.estudiantes.entity.Estudiante;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Set;

/**
 * Mapper Entity <-> DTO para Estudiante y sus relaciones (Documento,
 * ContactoEmergencia).
 *
 * <p>Implementacion generada por MapStruct en {@code target/generated-sources/}.
 * Spring la inyecta como bean (@{@link Mapper} con
 * {@code componentModel="spring"}).</p>
 *
 * <p>Patron del proyecto: este es el primer mapper; replicar la misma estructura
 * para los siguientes MS (instructores, vehiculos, etc.).</p>
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface EstudianteMapper {

    // ---------------------------------------------------------------------
    // Estudiante
    // ---------------------------------------------------------------------

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "estado", constant = "PRE_MATRICULADO")
    @Mapping(target = "fechaMatricula", ignore = true)
    @Mapping(target = "usuarioId", ignore = true)
    @Mapping(target = "documentos", ignore = true)
    @Mapping(target = "contactosEmergencia", source = "contactosEmergencia")
    Estudiante toEntity(CreateEstudianteRequest request);

    EstudianteResponse toResponse(Estudiante entity);

    EstudianteDetailResponse toDetailResponse(Estudiante entity);

    @Mapping(target = "nombreCompleto", expression = "java(entity.getNombre() + \" \" + entity.getApellido())")
    EstudianteListResponse toListResponse(Estudiante entity);

    List<EstudianteListResponse> toListResponse(List<Estudiante> entities);

    /**
     * Aplica los campos no-nulos del request sobre la entidad existente
     * (semantica PATCH: solo actualiza lo enviado).
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cedula", ignore = true)
    @Mapping(target = "usuarioId", ignore = true)
    @Mapping(target = "fechaMatricula", ignore = true)
    @Mapping(target = "documentos", ignore = true)
    @Mapping(target = "contactosEmergencia", ignore = true)
    void updateEntity(@MappingTarget Estudiante entity, UpdateEstudianteRequest request);

    // ---------------------------------------------------------------------
    // Documento
    // ---------------------------------------------------------------------

    DocumentoResponse toDocumentoResponse(Documento entity);

    // ---------------------------------------------------------------------
    // ContactoEmergencia
    // ---------------------------------------------------------------------

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "estudiante", ignore = true)
    ContactoEmergencia toEntity(ContactoEmergenciaRequest request);

    ContactoEmergenciaResponse toContactoResponse(ContactoEmergencia entity);

    Set<ContactoEmergencia> toContactoEntities(List<ContactoEmergenciaRequest> requests);
}
