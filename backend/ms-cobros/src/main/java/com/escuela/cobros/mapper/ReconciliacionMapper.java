package com.escuela.cobros.mapper;

import com.escuela.cobros.dto.ReconciliacionListResponse;
import com.escuela.cobros.dto.ReconciliacionRequest;
import com.escuela.cobros.dto.ReconciliacionResponse;
import com.escuela.cobros.entity.Reconciliacion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ReconciliacionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "totalDia", ignore = true)
    @Mapping(target = "usuarioConciliadorId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Reconciliacion toEntity(ReconciliacionRequest request);

    ReconciliacionResponse toResponse(Reconciliacion reconciliacion);

    ReconciliacionListResponse toListResponse(Reconciliacion reconciliacion);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "totalDia", ignore = true)
    @Mapping(target = "usuarioConciliadorId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntity(ReconciliacionRequest request, @MappingTarget Reconciliacion reconciliacion);
}
