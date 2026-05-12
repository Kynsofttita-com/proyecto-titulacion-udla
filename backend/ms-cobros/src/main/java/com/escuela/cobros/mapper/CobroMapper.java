package com.escuela.cobros.mapper;

import com.escuela.cobros.dto.CreateCobroRequest;
import com.escuela.cobros.dto.UpdateCobroRequest;
import com.escuela.cobros.dto.CobroListResponse;
import com.escuela.cobros.dto.CobroResponse;
import com.escuela.cobros.entity.Cobro;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CobroMapper {

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "estado", ignore = true)
    Cobro toEntity(CreateCobroRequest request);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updateEntity(UpdateCobroRequest request, @MappingTarget Cobro entity);

    @Mapping(target = "dateCreated", ignore = true)
    @Mapping(target = "dateUpdated", ignore = true)
    CobroResponse toResponse(Cobro entity);

    @Mapping(target = "dateCreated", ignore = true)
    CobroListResponse toListResponse(Cobro entity);
}
