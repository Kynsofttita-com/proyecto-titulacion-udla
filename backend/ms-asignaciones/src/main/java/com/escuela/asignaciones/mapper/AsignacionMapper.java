package com.escuela.asignaciones.mapper;

import com.escuela.asignaciones.dto.CreateAsignacionRequest;
import com.escuela.asignaciones.dto.UpdateAsignacionRequest;
import com.escuela.asignaciones.dto.AsignacionListResponse;
import com.escuela.asignaciones.dto.AsignacionResponse;
import com.escuela.asignaciones.entity.Asignacion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AsignacionMapper {

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaHora", source = "request", qualifiedByName = "buildFechaHora")
    @Mapping(target = "duracionMinutos", source = "request", qualifiedByName = "calculateDuracion")
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "tipoClase", ignore = true)
    @Mapping(target = "ubicacion", ignore = true)
    @Mapping(target = "motivoCancelacion", ignore = true)
    @Mapping(target = "version", ignore = true)
    Asignacion toEntity(CreateAsignacionRequest request);

    @Named("buildFechaHora")
    static LocalDateTime buildFechaHora(CreateAsignacionRequest request) {
        return request.fecha().atTime(request.horaInicio());
    }

    @Named("calculateDuracion")
    static Short calculateDuracion(CreateAsignacionRequest request) {
        return (short) ChronoUnit.MINUTES.between(request.horaInicio(), request.horaFin());
    }

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaHora", ignore = true)
    @Mapping(target = "duracionMinutos", ignore = true)
    @Mapping(target = "tipoClase", ignore = true)
    @Mapping(target = "ubicacion", ignore = true)
    @Mapping(target = "motivoCancelacion", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(UpdateAsignacionRequest request, @MappingTarget Asignacion entity);

    @Mapping(target = "fecha", ignore = true)
    @Mapping(target = "horaInicio", ignore = true)
    @Mapping(target = "horaFin", ignore = true)
    @Mapping(target = "dateCreated", ignore = true)
    @Mapping(target = "dateUpdated", ignore = true)
    AsignacionResponse toResponse(Asignacion entity);

    @Mapping(target = "fecha", ignore = true)
    @Mapping(target = "horaInicio", ignore = true)
    @Mapping(target = "horaFin", ignore = true)
    @Mapping(target = "dateCreated", ignore = true)
    AsignacionListResponse toListResponse(Asignacion entity);
}
